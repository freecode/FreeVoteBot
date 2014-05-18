package org.freecode.irc.votebot.modules.admin;

import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/22/13
 * Time: 10:32 PM
 */
@Component
public class JoinChannelModule extends AdminModule {
	@Override
	public void processMessage(Privmsg privmsg) {
		String msg = privmsg.getMessage().substring(2).trim();
		privmsg.getConversable().getServer().joinChannel(msg);
	}

	@Override
	public String getName() {
		return "j";
	}

	@Override
	public String getParameterRegex() {
		return ".+";
	}

	protected Right[] getRights() {
		return new Right[]{Right.SOP, Right.FOUNDER};
	}
}
