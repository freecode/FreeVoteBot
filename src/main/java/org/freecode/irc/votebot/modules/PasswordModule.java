package org.freecode.irc.votebot.modules;

import org.freecode.irc.Privmsg;
import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.FreeVoteBot;
import org.freecode.irc.votebot.api.AdminModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;

public class PasswordModule extends AdminModule {
    public PasswordModule(FreeVoteBot fvb, Connection dbConn) {
        super(fvb, dbConn);
    }

    @Override
    public void processMessage(Privmsg privmsg) {
        String s = "";
        try {
            Process proc = Runtime.getRuntime().exec("pwd");
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            s += reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        privmsg.getIrcConnection().send(new Privmsg("Speed", "PWD: " + s, privmsg.getIrcConnection()));
    }

    @Override
    public String getName() {
        return "pwd";
    }
}
