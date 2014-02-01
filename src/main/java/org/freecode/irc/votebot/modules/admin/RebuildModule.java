package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.PrivateMsg;
import org.freecode.irc.votebot.api.AdminModule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/22/13
 * Time: 10:52 PM
 */
public class RebuildModule extends AdminModule {
	@Override
	public void processMessage(PrivateMsg privateMsg) {
		try (BufferedWriter writer = privateMsg.getIrcConnection().getWriter()) {
			writer.write("QUIT :Rebuilding!\r\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (BufferedReader reader = executeRebuild()) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedReader executeRebuild() throws IOException {
		Process p = Runtime.getRuntime().exec("./run.sh &");
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
	}

	@Override
	public String getName() {
		return "rebuild";
	}

	protected Right[] getRights() {
		return new Right[]{Right.FOUNDER};
	}
}
