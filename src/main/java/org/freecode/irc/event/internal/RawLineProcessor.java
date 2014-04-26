package org.freecode.irc.event.internal;

import org.freecode.irc.IrcConnection;

/**
 * User: Shivam
 * Date: 17/06/13
 * Time: 00:53
 */
public abstract class RawLineProcessor extends RawIrcListener {

    public RawLineProcessor(IrcConnection connection) {
        super(connection);
    }
}
