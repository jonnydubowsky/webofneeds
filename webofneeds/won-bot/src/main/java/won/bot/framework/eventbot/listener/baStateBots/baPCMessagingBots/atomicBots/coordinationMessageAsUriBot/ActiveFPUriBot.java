package won.bot.framework.eventbot.listener.baStateBots.baPCMessagingBots.atomicBots.coordinationMessageAsUriBot;

import won.bot.framework.eventbot.listener.baStateBots.BATestBotScript;
import won.bot.framework.eventbot.listener.baStateBots.BATestScriptAction;
import won.bot.framework.eventbot.listener.baStateBots.NopAction;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Danijel
 * Date: 17.4.14.
 */
public class ActiveFPUriBot extends BATestBotScript
{

  @Override
  protected List<BATestScriptAction> setupActions() {
    List<BATestScriptAction> actions = new ArrayList();

    actions.add(new NopAction());

    return actions;
  }
}
