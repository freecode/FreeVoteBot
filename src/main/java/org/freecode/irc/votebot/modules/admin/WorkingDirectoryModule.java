package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.PrivateMsg;
import org.freecode.irc.votebot.api.AdminModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WorkingDirectoryModule extends AdminModule {
	@Override
	public void processMessage(PrivateMsg privateMsg) {
		try (BufferedReader reader = executePwd()) {
			privateMsg.getIrcConnection().send(new PrivateMsg(privateMsg.getNick(), "PWD: " + reader.readLine(), privateMsg.getIrcConnection()));
		} catch (IOException e) {
			e.printStackTrace();
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
