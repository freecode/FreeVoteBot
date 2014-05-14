package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.CommandModule;

public class VersionModule extends CommandModule {
    private String version;
    private String commitMessage;
    private String commitAuthor;

    @Override
    public void processMessage(Privmsg privmsg) {
        privmsg.send("Version: " + version + ", last commit \"" + commitMessage + "\" by " + commitAuthor);
    }

    @Override
    public String getName() {
        return "version";
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public void setCommitAuthor(String commitAuthor) {
        this.commitAuthor = commitAuthor;
    }
}
