package com.speed.irc.event.api;

import com.speed.irc.connection.Server;
import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.IRCEventListener;

/**
 * Provides events for many API features.
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
public class ApiEvent implements IRCEvent {
    public static final int SERVER_DISCONNECTED = 1, EXCEPTION_RECEIVED = 2;

    public static final int SERVER_QUIT = 3;

    private int opcode;
    private Server server;
    private Object source;

    public ApiEvent(final int opcode, final Server server, final Object src) {
        source = src;
        this.opcode = opcode;
        this.server = server;
    }

    /**
     * Gets the opcode of the event.
     *
     * @return the opcode of this event
     * @see {@link ApiEvent#SERVER_DISCONNECTED},
     * {@link ApiEvent#EXCEPTION_RECEIVED}, {@link ApiEvent#SERVER_QUIT}
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * Gets the server from which the event was thrown.
     *
     * @return the server from which the event was thrown
     */
    public Server getServer() {
        return server;
    }

    public Object getSource() {
        return source;
    }

    public void callListener(final IRCEventListener listener) {
        if (listener instanceof ApiListener) {// these checks SHOULDN'T be
            // necessary
            ((ApiListener) listener).apiEventReceived(this);
        }
    }

}
