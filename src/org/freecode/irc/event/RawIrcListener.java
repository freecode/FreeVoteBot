package org.freecode.irc.event;

/**
 * User: Shivam
 * Date: 16/06/13
 * Time: 23:09
 */
public interface RawIrcListener {

    public boolean qualifies(final String rawLine);

    public void execute(final String rawLine);
}
