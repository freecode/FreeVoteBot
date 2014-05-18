package org.freecode.irc.votebot.modules.common;

import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.api.CommandModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VersionModule extends CommandModule {

    @Value("${git.commit.id.describe}")
    private String idDescribe;
    @Value("${git.commit.message.full}")
    private String commitMessage;
    @Value("${git.commit.user.name}")
    private String commitAuthor;
    @Value("${git.commit.time}")
    private String commitTime;

    @Override
    public void processMessage(Privmsg privmsg) {
        String[] params = privmsg.getMessage().split(" ");
        if (params.length == 1) {
            privmsg.getConversable().sendMessage("Version: " + idDescribe);
        } else {
            privmsg.getConversable().sendMessage("Version: " + idDescribe + ", last commit \"" +
                    commitMessage + "\" by " + commitAuthor + ", " + commitTime);
        }
    }

    @Override
    public String getName() {
        return "version";
    }

    @Override
    protected String getParameterRegex() {
        return "(full)?";
    }

    public void setIdDescribe(String idDescribe) {
        this.idDescribe = idDescribe;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public void setCommitAuthor(String commitAuthor) {
        this.commitAuthor = commitAuthor;
    }

    public void setCommitTime(String commitTime) {
        this.commitTime = commitTime;
    }
}

