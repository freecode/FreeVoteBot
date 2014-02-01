package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.PrivateMsg;
import org.freecode.irc.votebot.api.CommandModule;

public class TestModule extends CommandModule {
    @Override
    public void processMessage(PrivateMsg privateMsg) {
        String lastSender;
        if ((lastSender = getProperty(this.getClass(), "sender.last")) == null) {
            privateMsg.send("Successful test!");
        } else {
            //lastSender = new Gson().fromJson(lastSender, String.class);
            privateMsg.send("Successful test! Last tester was: " + lastSender);
        }
        storeProperty(this.getClass(), "sender.last", privateMsg.getNick());
    }

    public String getName() {
        return "test";
    }
}
