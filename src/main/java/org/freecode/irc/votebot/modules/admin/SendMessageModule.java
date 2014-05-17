package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/22/13
 * Time: 9:48 PM
 */
public class SendMessageModule extends AdminModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageModule.class);

	@Override
	public void processMessage(Privmsg privmsg) {
		String message = privmsg.getMessage().substring(4).trim();

		String[] split = message.split(" ", 2);
		String target = split[0];
		message = split[1];

        if(message.trim().isEmpty()) {
            return;
        }

        LOGGER.info("Sending message to " + target + ": " + message);
		privmsg.getIrcConnection().send(new Privmsg(target, message, privmsg.getIrcConnection()));
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
