package org.freecode.irc.votebot.api;

import org.freecode.irc.Privmsg;
import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.FreeVoteBot;

import java.sql.Connection;

public abstract class AdminModule extends CommandModule {
    public AdminModule(FreeVoteBot fvb, Connection dbConn) {
        super(fvb, dbConn);
    }

    @Override
    public boolean canRun(Transmittable trns) {
        if (super.canRun(trns)) {
            String host = ((Privmsg) trns).getHost();
            for (String admin_host : FreeVoteBot.ADMIN_HOSTS) {
                if (host.equals(admin_host)) {
                    return true;
                }
            }
        }
        return false;
    }
}
