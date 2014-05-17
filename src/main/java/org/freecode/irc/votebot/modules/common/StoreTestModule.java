package org.freecode.irc.votebot.modules.common;

import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.api.CommandModule;

public class StoreTestModule extends CommandModule {
    @Override
    public void processMessage(Privmsg privmsg) {
        String command = privmsg.getMessage().substring(getName().length() + 1).trim();

        if (command.equalsIgnoreCase("test")) {
            String lastSender;
            if ((lastSender = readString("sender.last")) == null) {
                privmsg.getConversable().sendMessage("Successful test!");
            } else {
				privmsg.getConversable().sendMessage("Successful test! Last tester was: " + lastSender);
            }
            store("sender.last", privmsg.getSender());
        } else if (command.startsWith("set ")) {
            try {
                String[] args = command.substring(4).trim().split(" ");
                String key = args[0];
                String value = args[1];

                store(key, value);
				privmsg.getConversable().sendMessage(key + ": " + readJson(key));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (command.startsWith("get ")) {
            try {
                String key = command.substring(4).trim();

                privmsg.getConversable().sendMessage(key + ": " + readJson(key));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return "kv";
    }
}
