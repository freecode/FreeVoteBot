package org.freecode.irc.votebot.modules.common;

import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.api.CommandModule;
import org.springframework.stereotype.Component;

@Component
public class TestModule extends CommandModule {
    @Override
    public void processMessage(Privmsg privmsg) {
        String lastSender;
        if ((lastSender = getStringProperty("sender.last")) == null) {
			privmsg.getConversable().sendMessage("Successful test!");
        } else {
            //lastSender = new Gson().fromJson(lastSender, String.class);
			privmsg.getConversable().sendMessage("Successful test! Last tester was: " + lastSender);
        }
        store("sender.last", privmsg.getSender());
    }

    public String getName() {
        return "test";
    }
}
