package org.freecode.irc.event.internal;

import org.freecode.irc.IrcConnection;

/**
 * User: Shivam
 * Date: 16/06/13
 * Time: 23:09
 */
public abstract class RawIrcListener {

    protected IrcConnection connection;

    public RawIrcListener(IrcConnection connection) {
        this.connection = connection;
    }

    public abstract boolean qualifies(final String rawLine);

    public abstract void execute(final String rawLine);
}
