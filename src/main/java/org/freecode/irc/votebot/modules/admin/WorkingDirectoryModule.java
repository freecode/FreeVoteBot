package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WorkingDirectoryModule extends AdminModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkingDirectoryModule.class);

	@Override
	public void processMessage(Privmsg privmsg) {
		try (BufferedReader reader = executePwd()) {
			privmsg.getIrcConnection().send(new Privmsg(privmsg.getNick(), "PWD: " + reader.readLine(), privmsg.getIrcConnection()));
		} catch (IOException e) {
            LOGGER.error("Failed to print working directory.", e);
		}
	}

	private BufferedReader executePwd() throws IOException {
		Process p = Runtime.getRuntime().exec("pwd");
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
	}

	@Override
	public String getName() {
		return "pwd";
	}

	protected Right[] getRights() {
		return new Right[]{Right.SOP, Right.FOUNDER, Right.AOP};
	}
}
