package won.bot.framework.eventbot.action.impl.mail.receive;

import won.bot.framework.bot.context.BotContext;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.mail.model.SubscribeStatus;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.mail.CreateNeedFromMailEvent;
import won.bot.framework.eventbot.event.impl.mail.MailCommandEvent;
import won.bot.framework.eventbot.event.impl.mail.MailReceivedEvent;
import won.bot.framework.eventbot.event.impl.mail.WelcomeMailEvent;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

/**
 * Created by fsuda on 30.09.2016.
 */
public class MailParserAction extends BaseEventBotAction {

    private MailContentExtractor mailContentExtractor;

    public MailParserAction(EventListenerContext eventListenerContext, MailContentExtractor mailContentExtractor) {
        super(eventListenerContext);
        this.mailContentExtractor = mailContentExtractor;
    }

    protected void doRun(Event event) throws Exception {
        if(event instanceof MailReceivedEvent){
            EventBus bus = getEventListenerContext().getEventBus();
            MimeMessage message = ((MailReceivedEvent) event).getMessage();
            String senderMailAddress = MailContentExtractor.getMailSender(message);

            try {
                if (mailContentExtractor.isCreateNeedMail(message)) {
                    processCreateNeedMail(message);
                } else if (mailContentExtractor.isCommandMail(message)) {

                    logger.debug("received a command mail publishing the MailCommand event");
                    bus.publish(new MailCommandEvent(message));

                } else {
                    logger.warn("unknown mail from user '{}' with subject '{}', no further processing required",
                                senderMailAddress, message.getSubject());
                }
            } catch (MessagingException me) {
                logger.error("Messaging exception occurred while processing MimeMessage: {}", me);
                logger.warn("mail from user '{}' with subject '{}' could not be processed",
                            senderMailAddress, message.getSubject());
            }
        }
    }

    private void processCreateNeedMail(MimeMessage message) throws MessagingException, IOException {

        EventBus bus = getEventListenerContext().getEventBus();
        BotContext botContext = getEventListenerContext().getBotContext();
        String senderMailAddress = MailContentExtractor.getMailSender(message);
        SubscribeStatus subscribeStatus =
          EventBotActionUtils.getSubscribeStatusForMailAddress(botContext, senderMailAddress);

        // depending of the user has subscribed/unsubscribed (via mailto links) his mails will be
        // published as needs, discarded or cached
        if (SubscribeStatus.SUBSCRIBED.equals(subscribeStatus)) {
            logger.info("received a create mail from subscribed user '{}' with subject '{}' so publish as need",
                        senderMailAddress, message.getSubject());
            bus.publish(new CreateNeedFromMailEvent(message));
        } else if (SubscribeStatus.UNSUBSCRIBED.equals(subscribeStatus)) {
            logger.info("received mail from unsubscribed user '{}' so discard mail with subject '{}'",
                        senderMailAddress, message.getSubject());
        } else {
            logger.info(
              "received a create mail from new user '{}' with subject '{}' so cache it and send welcome mail",
              senderMailAddress, message.getSubject());
            EventBotActionUtils.addCachedMailsForMailAddress(botContext, message);
            bus.publish(new WelcomeMailEvent(message));
        }
    }
}