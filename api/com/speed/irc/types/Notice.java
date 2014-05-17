package com.speed.irc.types;

import com.speed.irc.connection.Server;

import java.util.Arrays;

/**
 * A wrapper class for NOTICE messages.
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
 * along with Speed's IRC API. If not, see {@link http://www.gnu.org/licenses/}.
 *
 * @author Shivam Mistry
 */
public class Notice {
    private final String message, sender, target;
    private final Server server;

    /**
     * @param message The actual message.
     * @param sender  The nick of the person who the notice was sent to/from.
     * @param target  The channel the notice was sent to/from.
     * @param server  The server the notice should be sent on.
     */
    public Notice(final String message, final String sender,
                  final String target, final Server server) {
        this.message = message;
        this.target = target;
        this.sender = sender;
        this.server = server;
    }

    /**
     * Gets the message parsed from the raw command.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the sender of the notice.
     *
     * @return the nick of the notice sender
     * @deprecated see {@link #getSenderNick()} instead
     */
    public String getSender() {
        return sender;
    }

    /**
     * Gets the nick of the sender
     *
     * @return the senders nick
     */
    public String getSenderNick() {
        return sender;
    }

    /**
     * Gets the target of the notice.
     *
     * @return the target of the notice.
     */
    public Conversable getTarget() {
        return Arrays.binarySearch(server.getChannelPrefix(), target.charAt(0)) >= 0 ? server.getChannel(target) : server
                .getUser(target.toLowerCase());
    }

    /**
     * Gets the channel the notice was sent to or from
     *
     * @return the channel the notice was sent to or from
     * @deprecated see {@link #getTarget()} instead
     */
    public String getChannel() {
        return target;
    }

    /**
     * Gets the server this notice was sent on.
     *
     * @return the server this notice was sent on.
     */
    public Server getServer() {
        return server;
    }
}
