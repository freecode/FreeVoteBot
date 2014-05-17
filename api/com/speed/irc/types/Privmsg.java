package com.speed.irc.types;

import java.util.Arrays;

/**
 * A wrapper class for PRIVMSGs.
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
public class Privmsg {

	private final String message, sender;
	private Conversable conversable;

	/**
	 * @param message     The actual message.
	 * @param sender      The nick of the person who the message was sent to/from.
	 * @param conversable The channel the message was sent to/from.
	 */
	public Privmsg(final String message, final String sender,
				   final Conversable conversable) {
		this.message = message;
		this.conversable = conversable;
		this.sender = sender;
	}


	/*
	 * Gets the message sent
     *
     * @return the message
     */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the sender
	 *
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * Gets the conversable object of the sender/channel
	 *
	 * @return the conversable object
	 */
	public Conversable getConversable() {
		return conversable;
	}

	/**
	 * Checks whether the message is a private message
	 *
	 * @return true if the message was sent privately, false otherwise
	 */
	public boolean isPrivateMessage() {
		return !isChannelMessage();
	}

	/**
	 * Checks whether the message is a channel message
	 *
	 * @return <tt>true</tt> if the message was sent to a channel,
	 * <tt>false</tt> otherwise
	 */
	public boolean isChannelMessage() {
		return Arrays.binarySearch(getConversable().getServer().getChannelPrefix(), getConversable().getName().charAt(0)) >= 0;
	}

	/**
	 * Check if the message is a CTCP request/reply
	 *
	 * @return true if the message was a CTCP request/reply
	 */
	public boolean isCtcpMessage() {
		return message.startsWith("\u0001");
	}

}
