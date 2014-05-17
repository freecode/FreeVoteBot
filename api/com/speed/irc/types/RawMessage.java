package com.speed.irc.types;

import com.speed.irc.connection.Server;

/**
 * Represents a raw message.
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
public class RawMessage {

    private String raw, code, sender, target;
    private Server server;

    /**
     * Initialises a wrapper for raw messages.
     *
     * @param raw    the raw message
     * @param server the server the raw message was sent from
     */
    public RawMessage(String raw, final Server server) {
        this.raw = raw;
        this.server = server;
        String[] strings = raw.split(" ");
        code = strings[1];
        sender = strings[0];
        if (strings.length > 2)
            target = strings[2];

    }

    /**
     * Gets the raw message.
     *
     * @return the raw message.
     */
    public String getRaw() {
        return raw;
    }

    /**
     * Gets the command/code of this raw message.
     *
     * @return the command or code of this raw message.
     */
    public String getCommand() {
        return code;
    }

    /**
     * Gets the target of this raw message.
     *
     * @return the target of the message.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Gets the sender of the message
     *
     * @return the sender of the message, in the form <tt>*!*@*</tt>
     */
    public String getSender() {
        return sender;
    }

    /**
     * Gets the server the raw message was sent on.
     *
     * @return the server the raw message was sent on.
     */
    public Server getServer() {
        return server;
    }

}
