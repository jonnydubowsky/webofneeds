package won.bot.framework.eventbot.action.impl.mail.receive;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.mail.model.ActionType;
import won.bot.framework.eventbot.action.impl.mail.model.SubscribeStatus;
import won.bot.framework.eventbot.action.impl.mail.model.WonURI;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.SendTextMessageOnConnectionEvent;
import won.bot.framework.eventbot.event.impl.mail.CloseConnectionEvent;
import won.bot.framework.eventbot.event.impl.mail.MailCommandEvent;
import won.bot.framework.eventbot.event.impl.mail.OpenConnectionEvent;
import won.bot.framework.eventbot.event.impl.mail.SubscribeUnsubscribeEvent;
import won.protocol.util.WonRdfUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

/**
 * Created by fsuda on 18.10.2016.
 */
public class MailCommandAction extends BaseEventBotAction {

    private String mailIdUriRelationsName;
    private MailContentExtractor mailContentExtractor;

    public MailCommandAction(EventListenerContext eventListenerContext, String mailIdUriRelationsName,
                             MailContentExtractor mailContentExtractor) {
        super(eventListenerContext);
        this.mailIdUriRelationsName = mailIdUriRelationsName;
        this.mailContentExtractor = mailContentExtractor;
    }

    @Override
    protected void doRun(Event event) throws Exception {

        if(event instanceof MailCommandEvent) {

            MimeMessage message = ((MailCommandEvent) event).getMessage();
            String referenceId = MailContentExtractor.getMailReference(message);

            // determine if the mail is referring to some other mail/need/connection or not
            if (referenceId != null) {
                processReferenceMailCommands(message, referenceId);
            } else {
                processNonReferenceMailCommand(message);
            }
        }
    }

    private void processNonReferenceMailCommand(MimeMessage message) throws IOException, MessagingException {

        EventBus bus = getEventListenerContext().getEventBus();
        ActionType mailAction = mailContentExtractor.getMailAction(message);

        switch(mailAction) {
            case SUBSCRIBE:
                bus.publish(new SubscribeUnsubscribeEvent(message, SubscribeStatus.SUBSCRIBED));
                break;

            case UNSUBSCRIBE:
                bus.publish(new SubscribeUnsubscribeEvent(message, SubscribeStatus.UNSUBSCRIBED));
                break;

            case NO_ACTION:
            default:
                //INVALID COMMAND
                logger.error("No command was given or assumed");
                break;
        }
    }

    private void processReferenceMailCommands(MimeMessage message, String referenceId) {

        EventBus bus = getEventListenerContext().getEventBus();
        try{
            WonURI wonUri = EventBotActionUtils.getWonURIForMailId(getEventListenerContext(), mailIdUriRelationsName, referenceId);
            assert wonUri != null;

            ActionType actionType = determineAction(getEventListenerContext(), message, wonUri);
            logger.debug("Executing " + actionType + " on uri: " + wonUri.getUri() + " of type " + wonUri.getType());

            switch(actionType) {
                case CLOSE_CONNECTION:
                    bus.publish(new CloseConnectionEvent(wonUri.getUri()));
                    break;
                case OPEN_CONNECTION:
                    bus.publish(new OpenConnectionEvent(wonUri.getUri()));
                    break;
                case IMPLICIT_OPEN_CONNECTION:
                    bus.publish(new OpenConnectionEvent(
                      wonUri.getUri(),  mailContentExtractor.getTextMessage(message)));
                    break;
                case SENDMESSAGE:
                    bus.publish(new SendTextMessageOnConnectionEvent(
                      mailContentExtractor.getTextMessage(message), wonUri.getUri()));
                    break;

                case NO_ACTION:
                default:
                    //INVALID COMMAND
                    logger.error("No command was given or assumed");
                    break;
            }
        }catch(Exception e){
            logger.error("no reply mail was set or found");
        }
    }

    private ActionType determineAction(EventListenerContext ctx, MimeMessage message, WonURI wonUri) {
        try {

            ActionType mailAction = mailContentExtractor.getMailAction(message);
            switch(wonUri.getType()) {
                case CONNECTION:
                    boolean connected = WonRdfUtils.ConnectionUtils.isConnected(
                      ctx.getLinkedDataSource().getDataForResource(wonUri.getUri()), wonUri.getUri());
                    if (ActionType.CLOSE_CONNECTION.equals(mailAction)) {
                        return ActionType.CLOSE_CONNECTION;
                    }else if(!connected && ActionType.OPEN_CONNECTION.equals(mailAction)){
                        return ActionType.OPEN_CONNECTION;
                    }else if(connected){
                        return ActionType.SENDMESSAGE;
                    }else{
                        //if the connection is not connected yet and we do not parse any command we assume that the mailsender wants to establish a connection
                        return ActionType.IMPLICIT_OPEN_CONNECTION;
                    }
                case NEED:
                    //TODO: implement need actions like close/reopen etc.
                default:
                    return mailAction;
            }
        }catch(MessagingException me){
            logger.error("exception occurred checking command mail: {}", me);
            return ActionType.NO_ACTION;
        }catch(IOException ioe){
            logger.error("exception occurred checking command mail: {}", ioe);
            return ActionType.NO_ACTION;
        }
    }
}