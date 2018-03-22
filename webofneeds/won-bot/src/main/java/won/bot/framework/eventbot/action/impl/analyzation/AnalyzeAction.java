/*
 * Copyright 2017  Research Studios Austria Forschungsges.m.b.H.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package won.bot.framework.eventbot.action.impl.analyzation;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import won.bot.framework.bot.context.FactoryBotContextWrapper;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.impl.factory.model.Precondition;
import won.bot.framework.eventbot.action.impl.factory.model.Proposal;
import won.bot.framework.eventbot.action.impl.factory.model.ProposalState;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.*;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.AgreementCanceledEvent;
import won.bot.framework.eventbot.event.impl.analyzation.agreement.ProposalAcceptedEvent;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionMetEvent;
import won.bot.framework.eventbot.event.impl.analyzation.precondition.PreconditionUnmetEvent;
import won.bot.framework.eventbot.event.impl.analyzation.proposal.ProposalReceivedEvent;
import won.bot.framework.eventbot.event.impl.command.connectionmessage.ConnectionMessageCommandEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.WonMessageReceivedOnConnectionEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.WonMessageSentOnConnectionEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.agreement.AgreementProtocolState;
import won.protocol.agreement.effect.*;
import won.protocol.message.WonMessage;
import won.protocol.model.Connection;
import won.protocol.util.NeedModelWrapper;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.LinkedDataSource;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.utils.goals.GoalInstantiationProducer;
import won.utils.goals.GoalInstantiationResult;

import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

public class AnalyzeAction extends BaseEventBotAction {

    public AnalyzeAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        logger.trace("Analyzing Message...");
        EventListenerContext ctx = getEventListenerContext();
        EventBus bus = ctx.getEventBus();

        if(!(ctx.getBotContextWrapper() instanceof FactoryBotContextWrapper)) {
            logger.error("AnalyzeAction can only work with a FactoryBotContextWrapper, but was an instance of class: " + ctx.getBotContextWrapper().getClass());
            return ;
        }

        FactoryBotContextWrapper botContextWrapper = (FactoryBotContextWrapper) ctx.getBotContextWrapper();
        LinkedDataSource linkedDataSource = ctx.getLinkedDataSource();

        boolean receivedMessage;

        if(event instanceof WonMessageSentOnConnectionEvent) {
            receivedMessage = false;
        } else if(event instanceof WonMessageReceivedOnConnectionEvent) {
            receivedMessage = true;
        } else {
            logger.error("AnalyzeAction can only handle WonMessageReceivedOnConnectionEvent or WonMessageSentOnConnectionEvent, was an event of class: " + event.getClass());
            return;
        }

        URI needUri = ((NeedSpecificEvent) event).getNeedURI();
        URI remoteNeedUri = ((RemoteNeedSpecificEvent) event).getRemoteNeedURI();
        URI connectionUri = ((ConnectionSpecificEvent) event).getConnectionURI();
        Connection connection = makeConnection(needUri, remoteNeedUri, connectionUri);
        WonMessage wonMessage = ((MessageEvent) event).getWonMessage();
        logger.trace("Message Information ------");
        logger.trace("Message Type: "+ (receivedMessage ? "RECEIVED" : "SENT"));
        logger.trace("NeedUri: " + needUri);
        logger.trace("remoteNeedUri: " + remoteNeedUri);
        logger.trace("connectionUri: " + connectionUri);

        if(connectionUri == null || WonRdfUtils.MessageUtils.isProcessingMessage(wonMessage)){
            logger.debug("AnalyzeAction will not execute on processing messages or messages without a connectionUri (e.g. connect messages)");
            logger.trace("--------------------------");
            return;
        }

        if(receivedMessage) {
            publishAnalyzingMessage(connection);
        }

        Dataset needDataset = linkedDataSource.getDataForResource(needUri);
        Collection<Resource> goalsInNeed = new NeedModelWrapper(needDataset).getGoals();
        logger.trace("Preconditions in Need: " + goalsInNeed.size());

        AgreementProtocolState agreementProtocolState = AgreementProtocolState.of(connectionUri, getEventListenerContext().getLinkedDataSource()); //Initialize with null, to ensure some form of lazy init for the agreementProtocolState
        Set<MessageEffect> messageEffects = agreementProtocolState.getEffects(wonMessage.getMessageURI());

        logger.trace("MessageEffects in Message: "+messageEffects.size()+"\n");

        messageEffects.forEach(messageEffect -> {
            if(messageEffect instanceof Accepts) {
                logger.trace("\tMessageEffect 'Accepts':");
                if(receivedMessage) {
                    Accepts effect = (Accepts) messageEffect;

                    effect.getCancelledAgreementURIs().forEach(cancelledAgreementUri -> {
                        bus.publish(new AgreementCanceledEvent(connection, cancelledAgreementUri));
                    });

                    Model agreementPayload = agreementProtocolState.getAgreement(effect.getAcceptedMessageUri());

                    if (!agreementPayload.isEmpty()) {
                        bus.publish(new ProposalAcceptedEvent(connection, effect.getAcceptedMessageUri(), agreementPayload));
                    }
                }
            } else if(messageEffect instanceof Proposes) {
                logger.trace("\tMessageEffect 'Proposes':");
                Proposal proposal = new Proposal(messageEffect.getMessageUri(), ProposalState.SUGGESTED);
                Model proposalModel = agreementProtocolState.getPendingProposal(proposal.getUri());

                if(!proposalModel.isEmpty()) {
                    logger.trace("\t\tProposal: " + proposal);
                    for(Resource goal : goalsInNeed){
                        String preconditionUri = getUniqueGoalId(goal, needDataset, connectionUri);
                        logger.trace("\t\t\tPreconditionUri: "+preconditionUri);

                        if(!botContextWrapper.hasPreconditionProposalRelation(preconditionUri, proposal.getUri().toString())) {
                            GoalInstantiationResult result = GoalInstantiationProducer.findInstantiationForGoalInDataset(needDataset, goal, proposalModel);
                            Precondition precondition = new Precondition(preconditionUri, result.isConform()); //TODO: GOAL INSTANTIATION PRODUCER DUPLICATES THE DATA SOMEHOW AND THUS MAKING THE INITIALLY CONFORM GOAL NOT MET

                            logger.trace("\t\t\tPrecondition: " + precondition);


                            //TODO: WE MIGHT NEED TO CHECK WHETHER THE PRECONDITION IS ACTUALLY FULFILLED OR NOT BEFORE WE REMOVE THE TEMP STATUS
                            boolean preconditionMetPending = botContextWrapper.isPreconditionMetInProposals(precondition.getUri());
                            logger.trace("\t\t\tRemove PreconditionMetPending Entry: "+preconditionMetPending);

                            if(preconditionMetPending) {
                                botContextWrapper.removePreconditionMetPending(precondition.getUri());
                            }

                            logger.trace("\t\t\tAdding Precondition/Proposal Relation");
                            botContextWrapper.addPreconditionProposalRelation(precondition, proposal);
                        }else{
                            logger.trace("\t\t\tPrecondition/Proposal Relation already present");
                        }
                    }

                    if(receivedMessage) {
                        logger.trace("\t\tSend ProposalReceivedEvent");
                        WonMessageReceivedOnConnectionEvent receivedOnConnectionEvent = (WonMessageReceivedOnConnectionEvent) event;
                        bus.publish(new ProposalReceivedEvent(connection, receivedOnConnectionEvent));
                    }
                } else {
                    logger.trace("\t\tProposal: EMPTY");
                }
            } else if(messageEffect instanceof Rejects) {
                logger.trace("\tMessageEffect 'Rejects':");
                Rejects effect = (Rejects) messageEffect;

                logger.trace("\t\tremove Proposal References for: "+effect.getRejectedMessageUri());
                botContextWrapper.removeProposalReferences(effect.getRejectedMessageUri());
            } else if(messageEffect instanceof Retracts) {
                logger.trace("\tMessageEffect 'Retracts':");
                Retracts effect = (Retracts) messageEffect;

                logger.trace("\t\tremove Proposal References for: "+effect.getRetractedMessageUri());
                botContextWrapper.removeProposalReferences(effect.getRetractedMessageUri());
            }
        });
        logger.trace("--------------------------");

        //Things to do for each individual message regardless of it being received or sent
        Dataset remoteNeedDataset = ctx.getLinkedDataSource().getDataForResource(remoteNeedUri);
        Dataset conversationDataset = null;  //Initialize with null, to ensure some form of lazy init for the conversationDataset
        GoalInstantiationProducer goalInstantiationProducer = null;
        logger.trace("Conversation Information ------");

        for (Resource goal : goalsInNeed) {
            String preconditionUri = getUniqueGoalId(goal, needDataset, connectionUri);

            logger.trace("\tPreconditionUri: "+preconditionUri);

            if(botContextWrapper.isPreconditionMetInProposals(preconditionUri)){
                logger.trace("\t\tPrecondition already met in a proposal/agreement");
            } else if(botContextWrapper.isPreconditionMetPending(preconditionUri)){
                logger.trace("\t\tPrecondition already met by a pending proposal that does not exist yet");
            } else {
                logger.trace("\t\tPrecondition not yet met in a proposal/agreement");
                conversationDataset = getConversationDatasetLazyInit(conversationDataset, connectionUri);
                goalInstantiationProducer = getGoalInstantiationProducerLazyInit(goalInstantiationProducer, needDataset, remoteNeedDataset, conversationDataset);

                GoalInstantiationResult result = goalInstantiationProducer.findInstantiationForGoal(goal);
                Boolean oldGoalState = botContextWrapper.getPreconditionConversationState(preconditionUri);
                boolean newGoalState = result.getShaclReportWrapper().isConform();

                if(oldGoalState == null || newGoalState != oldGoalState) {
                    logger.trace("\t\t\tState changed");
                    botContextWrapper.addPreconditionConversationState(preconditionUri, newGoalState);
                    if(newGoalState) {
                        logger.trace("\t\t\t\tadding PreconditionMetPending");
                        botContextWrapper.addPreconditionMetPending(preconditionUri);
                        logger.trace("\t\t\t\tsending PreconditionMetEvent");
                        ctx.getEventBus().publish(new PreconditionMetEvent(connection, preconditionUri, result));
                    }else{
                        logger.trace("\t\t\t\tsending PreconditionUnmetEvent");
                        ctx.getEventBus().publish(new PreconditionUnmetEvent(connection, preconditionUri, result));
                    }
                }else{
                    logger.trace("\t\t\tNo state change");
                }
            }
        }

        if(receivedMessage){
            publishAnalyzingCompleteMessage(connection, null);
        }
    }

    //********* Helper Methods **********
    private Dataset getConversationDatasetLazyInit(Dataset conversationDataset, URI connectionUri) {
        if(conversationDataset == null){
            logger.debug("Retrieving Conversation Of Connection");
            return WonLinkedDataUtils.getConversationDataset(connectionUri, getEventListenerContext().getLinkedDataSource());
        }else{
            logger.debug("Already retrieved FullConversation Of Connection");
            return conversationDataset;
        }
    }

    private GoalInstantiationProducer getGoalInstantiationProducerLazyInit(GoalInstantiationProducer goalInstantiationProducer, Dataset needDataset, Dataset remoteNeedDataset, Dataset conversationDataset){
        if(goalInstantiationProducer == null){
            logger.debug("Instantiating GoalInstantiationProducer");
            return new GoalInstantiationProducer(needDataset, remoteNeedDataset, conversationDataset, "http://example.org/", "http://example.org/blended/");
        }else{
            logger.debug("Already instantiated GoalInstantiationProducer");
            return goalInstantiationProducer;
        }
    }

    private void publishAnalyzingMessage(Connection connection) {
        logger.trace("Publishing AnalyzingMessage for Connection: "+connection.getConnectionURI());
        Model messageModel = WonRdfUtils.MessageUtils.processingMessage(getEventListenerContext().getBotContextWrapper().getBotName() + " - Starting Analyzation");
        getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(connection, messageModel));
    }

    private void publishAnalyzingCompleteMessage(Connection connection, String detailMessage) {
        logger.trace("Publishing AnalyzingCompleteMessage for Connection: "+connection.getConnectionURI()+" DetailMessage: "+detailMessage);
        Model messageModel = WonRdfUtils.MessageUtils.processingMessage(getEventListenerContext().getBotContextWrapper().getBotName() + " - Analyzation complete" + (detailMessage!= null? (", DetailMessage: "+detailMessage): ""));
        getEventListenerContext().getEventBus().publish(new ConnectionMessageCommandEvent(connection, messageModel));
    }

    private static String getUniqueGoalId(Resource goal, Dataset needDataset, URI connectionURI) { //TODO: GOAL STATE RETRIEVAL IS NOT BASED ON THE CORRECT URI SO FAR
        if(goal.getURI() != null) {
            return goal.getURI();
        }else{
            NeedModelWrapper needWrapper = new NeedModelWrapper(needDataset);

            StringWriter writer = new StringWriter();
            Model shapesModel = needWrapper.getShapesGraph(goal);
            if(shapesModel != null) {
                shapesModel.write(writer, "TRIG");
            }
            Model dataModel = needWrapper.getDataGraph(goal);
            if(dataModel != null) {
                dataModel.write(writer, "TRIG");
            }

            return connectionURI +"#"+ writer.toString().replaceAll("\\R", " ");
        }
    }

    private static Connection makeConnection(URI needURI, URI remoteNeedURI, URI connectionURI){
        Connection con = new Connection();
        con.setConnectionURI(connectionURI);
        con.setNeedURI(needURI);
        con.setRemoteNeedURI(remoteNeedURI);
        return con;
    }
}