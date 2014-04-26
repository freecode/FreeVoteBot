package org.freecode.irc.event.internal;

import org.freecode.irc.CtcpRequest;
import org.freecode.irc.IrcConnection;
import org.freecode.irc.Privmsg;
import org.freecode.irc.event.CtcpRequestListener;
import org.freecode.irc.event.PrivateMessageListener;

/**
 * User: Shivam
 * Date: 16/06/13
 * Time: 23:57
 */
public class RawPrivateMessageProcessor extends RawLineProcessor {


    public RawPrivateMessageProcessor(IrcConnection connection) {
        super(connection);
    }

    public boolean qualifies(String rawLine) {
        final String[] parts = rawLine.split(" ", 4);
        return parts.length == 4 && parts[1].equals("PRIVMSG"); //&& !CtcpRequest.isCtcpRequest(rawLine); //&& parts[3].startsWith(":");
    }

    public void execute(String rawLine) {
        if (!CtcpRequest.isCtcpRequest(rawLine)) {
            final Privmsg privmsg = new Privmsg(rawLine, connection);
            for (PrivateMessageListener listener : connection.getDelegates(PrivateMessageListener.class)) {
                listener.onPrivmsg(privmsg);
            }
        } else {
            final CtcpRequest request = new CtcpRequest(rawLine, connection);
            for (CtcpRequestListener listener : connection.getDelegates(CtcpRequestListener.class)) {
                listener.onCtcpRequest(request);
            }
        }
    }
}
