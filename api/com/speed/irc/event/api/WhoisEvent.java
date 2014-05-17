package com.speed.irc.event.api;

import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.IRCEventListener;
import com.speed.irc.types.Whois;

/**
 * Represents a WHOIS event.
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
public class WhoisEvent implements IRCEvent {

    private final Whois whois;
    private final Object source;

    public WhoisEvent(final Whois whois, final Object source) {
        this.whois = whois;
        this.source = source;
    }

    /**
     * Gets the WHOIS object that this event represents, not guaranteed to be non-null.
     *
     * @return the event represented by this object, may be null
     */
    public Whois getWhois() {
        return whois;
    }

    public Object getSource() {
        return source;
    }

    @Override
    public void callListener(IRCEventListener listener) {
        if (listener instanceof WhoisListener) {
            ((WhoisListener) listener).whoisReceived(this);
        }
    }

}
