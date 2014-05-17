package org.freecode.irc.votebot.modules.admin;

import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/22/13
 * Time: 9:48 PM
 */
public class SendMessageModule extends AdminModule {
	@Override
	public void processMessage(Privmsg privmsg) {
		String msg = privmsg.getMessage().substring(4).trim();
		String[] split = msg.split(" ", 2);
		String target = split[0];
		msg = split[1];
        if(msg.trim().isEmpty()) {
            return;
        }
		privmsg.getConversable().getServer().
				sendMessage(new Privmsg(msg, null, privmsg.getConversable().getServer().getUser(target)));
	}

	@Override
	public String getName() {
		return "msg";
	}

	@Override
	public String getParameterRegex() {
		return ".+";
	}

	protected Right[] getRights() {
		return new Right[]{Right.SOP, Right.FOUNDER};
	}
}
