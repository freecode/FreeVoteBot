package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.CommandModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreTestModule extends CommandModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreTestModule.class);

    @Override
    public void processMessage(Privmsg privmsg) {
        String command = privmsg.getMessage().substring(getName().length() + 1).trim();

        if (command.equalsIgnoreCase("test")) {
            String lastSender;
            if ((lastSender = readString("sender.last")) == null) {
                privmsg.send("Successful test!");
            } else {
                privmsg.send("Successful test! Last tester was: " + lastSender);
            }
            store("sender.last", privmsg.getNick());
        } else if (command.startsWith("set ")) {
            try {
                String[] args = command.substring(4).trim().split(" ");
                String key = args[0];
                String value = args[1];

                store(key, value);
                privmsg.send(key + ": " + readJson(key));
            } catch (Exception e) {
                LOGGER.error("Failed to read Json.", e);
            }
        } else if (command.startsWith("get ")) {
            try {
                String key = command.substring(4).trim();

                privmsg.send(key + ": " + readJson(key));
            } catch (Exception e) {
                LOGGER.error("Failed to read Json.", e);
            }
        }
    }

    public String getName() {
        return "kv";
    }
}
