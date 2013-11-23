package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/22/13
 * Time: 10:32 PM
 */
public class JoinChannelModule extends AdminModule {
    @Override
    public void processMessage(Privmsg privmsg) {
        String msg = privmsg.getMessage().substring(2).trim();
        privmsg.getIrcConnection().joinChannel(msg);
    }

    @Override
    public String getName() {
        return "j";
    }

    @Override
    public String getParameterRegex() {
        return ".+";
    }
}
