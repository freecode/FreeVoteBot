package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.PrivateMsg;
import org.freecode.irc.votebot.api.AdminModule;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/22/13
 * Time: 10:32 PM
 */
public class JoinChannelModule extends AdminModule {
	@Override
	public void processMessage(PrivateMsg privateMsg) {
		String msg = privateMsg.getMessage().substring(2).trim();
		privateMsg.getIrcConnection().joinChannel(msg);
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
