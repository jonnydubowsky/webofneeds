package won.bot.framework.events.listener.baStateBots.baCCMessagingBots.atomicBots.coordinationMessageAsTextBot;

import won.bot.framework.events.listener.baStateBots.BATestBotScript;
import won.bot.framework.events.listener.baStateBots.BATestScriptAction;
import won.bot.framework.events.listener.baStateBots.NopAction;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Danijel
 * Date: 17.4.14.
 */
public class ActiveFPBot extends BATestBotScript
{

  @Override
  protected List<BATestScriptAction> setupActions() {
    List<BATestScriptAction> actions = new ArrayList();

    actions.add(new NopAction());

    return actions;
  }
}