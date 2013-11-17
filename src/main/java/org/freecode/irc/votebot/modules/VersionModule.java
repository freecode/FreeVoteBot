package org.freecode.irc.votebot.modules;

import org.freecode.irc.Privmsg;
import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.FreeVoteBot;
import org.freecode.irc.votebot.api.CommandModule;

import java.sql.Connection;

public class VersionModule extends CommandModule {
    public VersionModule(FreeVoteBot fvb) {
        super(fvb);
    }

    @Override
    public void processMessage(Privmsg privmsg) {
        privmsg.send("Version: " + FreeVoteBot.VERSION);
    }

    @Override
    public String getName() {
        return "version";
    }
}
