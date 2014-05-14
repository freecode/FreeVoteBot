package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.CommandModule;

public class TestModule extends CommandModule {
    @Override
    public void processMessage(Privmsg privmsg) {
        String lastSender;
        if ((lastSender = readString("sender.last")) == null) {
            privmsg.send("Successful test!");
        } else {
            //lastSender = new Gson().fromJson(lastSender, String.class);
            privmsg.send("Successful test! Last tester was: " + lastSender);
        }
        store("sender.last", privmsg.getNick());
    }

    public String getName() {
        return "test";
    }
}
