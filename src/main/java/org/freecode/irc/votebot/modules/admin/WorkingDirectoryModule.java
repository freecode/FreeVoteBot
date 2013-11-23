package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WorkingDirectoryModule extends AdminModule {
    @Override
    public void processMessage(Privmsg privmsg) {
        try  (BufferedReader reader = executePwd()) {
            privmsg.getIrcConnection().send(new Privmsg("Speed", "PWD: " + reader.readLine(), privmsg.getIrcConnection()));
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
}
