package org.freecode.irc;

/**
 * User: Shivam
 * Date: 17/06/13
 * Time: 00:48
 */
public class Privmsg {
    private final String target;
    private final String message;
    private final String senderMask;
    private final String user;
    private final String nick;
    private final String host;
    private final IrcConnection connection;

    public Privmsg(final String rawLine, final IrcConnection connection) {
        this.connection = connection;
        final String[] parts = rawLine.split(" ", 4);
        senderMask = parts[0];
        nick = senderMask.substring(0, senderMask.indexOf('!'));
        user = senderMask.substring(senderMask.indexOf('!') + 1, senderMask.indexOf('@'));
        host = senderMask.substring(senderMask.indexOf('@') + 1);
        message = parts[3].substring(1);
        target = parts[2];
    }

    public String getTarget() {
        return target;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderMask() {
        return senderMask;
    }

    public String getUser() {
        return user;
    }

    public String getNick() {
        return nick;
    }

    public String getHost() {
        return host;
    }

    public IrcConnection getIrcConnection() {
        return connection;
    }

}