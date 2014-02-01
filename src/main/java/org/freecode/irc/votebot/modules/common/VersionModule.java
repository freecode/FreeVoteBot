package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.CommandModule;

public class VersionModule extends CommandModule {
    private String version;

    @Override
    public void processMessage(Privmsg privmsg) {
        privmsg.send("Version: " + version);
    }

    @Override
    public String getName() {
        return "version";
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
