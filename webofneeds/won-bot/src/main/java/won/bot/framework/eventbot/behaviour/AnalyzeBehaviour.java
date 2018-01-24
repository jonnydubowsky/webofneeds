package won.bot.framework.eventbot.behaviour;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.impl.analyzation.AnalyzeAction;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherNeedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.OpenFromOtherNeedEvent;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;

import java.util.Optional;

/**
 * Created by fsuda on 28.11.2017.
 */
public class AnalyzeBehaviour extends BotBehaviour{
    public AnalyzeBehaviour(EventListenerContext context) {
        super(context);
    }

    public AnalyzeBehaviour(EventListenerContext context, String name) {
        super(context, name);
    }

    @Override
    protected void onActivate(Optional<Object> message) {
        this.subscribeWithAutoCleanup(MessageFromOtherNeedEvent.class,
                new ActionOnEventListener(
                        context,
                        new AnalyzeAction(context)
                )
        );
        this.subscribeWithAutoCleanup(OpenFromOtherNeedEvent.class,
                new ActionOnEventListener(
                        context,
                        new AnalyzeAction(context)
                )
        );
    }
}