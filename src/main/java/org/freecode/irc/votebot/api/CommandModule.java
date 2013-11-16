package org.freecode.irc.votebot.api;

import org.freecode.irc.Privmsg;
import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.FreeVoteBot;

import java.sql.Connection;

public abstract class CommandModule extends FVBModule {
    public CommandModule(FreeVoteBot fvb, Connection dbConn) {
        super(fvb, dbConn);
    }

    @Override
    public boolean canRun(Transmittable trns) {
        return trns instanceof Privmsg && ((Privmsg) trns).getMessage().equalsIgnoreCase("!" + getName());
    }

    @Override
    public void process(Transmittable trns) {
        processMessage((Privmsg) trns);
    }

    public abstract void processMessage(Privmsg privmsg);
}
