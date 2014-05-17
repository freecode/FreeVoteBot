package com.speed.irc.event.message;

import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.IRCEventListener;
import com.speed.irc.types.Privmsg;

/**
 * The wrapper class for an PRIVMSG event.
 * <p/>
 * This file is part of Speed's IRC API.
 * <p/>
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * <p/>
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Shivam Mistry
 */
public class PrivateMessageEvent implements IRCEvent {

    protected final Object source;
    protected final Privmsg message;

    public PrivateMessageEvent(final Privmsg message, final Object source) {
        this.source = source;
        this.message = message;
    }

    public Object getSource() {
        return source;
    }

    /**
     * Gets the message associated with this event.
     *
     * @return the message associated with this event.
     */
    public Privmsg getMessage() {
        return message;
    }

    public void callListener(IRCEventListener listener) {
        if (listener instanceof PrivateMessageListener) {
            ((PrivateMessageListener) listener)
                    .messageReceived(this);
        }
    }

}
