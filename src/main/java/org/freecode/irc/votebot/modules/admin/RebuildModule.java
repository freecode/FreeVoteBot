package org.freecode.irc.votebot.modules.admin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/22/13
 * Time: 10:52 PM
 */
public class RebuildModule extends AdminModule {
    @Override
    public void processMessage(Privmsg privmsg) {
        try {
	        BufferedWriter writer = privmsg.getIrcConnection().getWriter();
            writer.write("QUIT :Rebuilding!\r\n");
	        writer.flush();
	        writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Process p = Runtime.getRuntime().exec("./run.sh >> ./rebuild.log &");
            InputStreamReader reader = new InputStreamReader(p.getInputStream());
            BufferedReader read = new BufferedReader(reader);
            String line;
            while ((line = read.readLine()) != null) {
                System.out.println(line);
            }
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "rebuild";
    }
}
