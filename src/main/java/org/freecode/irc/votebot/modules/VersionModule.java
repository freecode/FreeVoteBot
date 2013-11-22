package org.freecode.irc.votebot.modules;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.FreeVoteBot;
import org.freecode.irc.votebot.api.CommandModule;

public class VersionModule extends CommandModule {
    @Override
    public void processMessage(Privmsg privmsg) {
        privmsg.send("Version: " + FreeVoteBot.VERSION);
    }

    @Override
    public String getName() {
        return "version";
    }
}
