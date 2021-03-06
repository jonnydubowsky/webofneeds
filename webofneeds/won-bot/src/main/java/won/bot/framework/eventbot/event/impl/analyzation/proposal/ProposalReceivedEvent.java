package won.bot.framework.eventbot.event.impl.analyzation.proposal;

import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.WonMessageReceivedOnConnectionEvent;
import won.protocol.model.Connection;

import java.net.URI;

/**
 * Created by fsuda on 08.03.2018.
 */
public class ProposalReceivedEvent extends ProposalEvent {
    public ProposalReceivedEvent(Connection con, WonMessageReceivedOnConnectionEvent proposalEvent) {
        super(con, proposalEvent);
    }
}
