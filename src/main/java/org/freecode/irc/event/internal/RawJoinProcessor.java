package org.freecode.irc.event.internal;

import org.freecode.irc.IrcConnection;
import org.freecode.irc.event.JoinListener;

/**
 * Created by shivam on 26/04/14.
 */
public class RawJoinProcessor extends RawIrcListener {

    public RawJoinProcessor(IrcConnection connection) {
        super(connection);
    }

    public boolean qualifies(String rawLine) {
        String[] parts = rawLine.split(" ", 3);
        return parts.length == 3 && parts[1].equalsIgnoreCase("join");
    }

    public void execute(String rawLine) {
        String[] parts = rawLine.split(" ", 3);
        String mask = parts[0];
        String channel = parts[2].replace(": ", "").trim();
        String nick = mask.contains("!") ? mask.split("!")[0] : mask;
        for (JoinListener listener : connection.getDelegates(JoinListener.class)) {
            listener.onJoin(channel, nick, mask);
        }

    }
}
