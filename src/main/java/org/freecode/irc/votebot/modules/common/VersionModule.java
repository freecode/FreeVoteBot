package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.PrivateMsg;
import org.freecode.irc.votebot.api.CommandModule;

public class VersionModule extends CommandModule {
    private String version;

    @Override
    public void processMessage(PrivateMsg privateMsg) {
        privateMsg.send("Version: " + version);
    }

    @Override
    public String getName() {
        return "version";
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
