package org.freecode.irc.votebot.modules.admin;

import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WorkingDirectoryModule extends AdminModule {
	@Override
	public void processMessage(Privmsg privmsg) {
		try (BufferedReader reader = executePwd()) {
			privmsg.getConversable().getServer().sendMessage(
					new Privmsg("PWD: " + reader.readLine(), null,
							privmsg.getConversable().getServer().getUser(privmsg.getSender())));
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
