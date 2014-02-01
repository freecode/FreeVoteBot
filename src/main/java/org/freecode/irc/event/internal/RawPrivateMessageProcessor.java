package org.freecode.irc.event.internal;

import org.freecode.irc.CtcpRequest;
import org.freecode.irc.IrcConnection;
import org.freecode.irc.PrivateMsg;
import org.freecode.irc.event.CtcpRequestListener;
import org.freecode.irc.event.PrivateMessageListener;

/**
 * User: Shivam
 * Date: 16/06/13
 * Time: 23:57
 */
public class RawPrivateMessageProcessor implements RawLineProcessor {
	private IrcConnection connection;

	public RawPrivateMessageProcessor(IrcConnection connection) {
		this.connection = connection;
	}

	public boolean qualifies(String rawLine) {
		final String[] parts = rawLine.split(" ", 4);
		return parts.length == 4 && parts[1].equals("PRIVMSG"); //&& !CtcpRequest.isCtcpRequest(rawLine); //&& parts[3].startsWith(":");
	}

	public void execute(String rawLine) {
		if (!CtcpRequest.isCtcpRequest(rawLine)) {
			final PrivateMsg privateMsg = new PrivateMsg(rawLine, connection);
			for (PrivateMessageListener listener : connection.getDelegates(PrivateMessageListener.class)) {
				listener.onPrivmsg(privateMsg);
			}
		} else {
			final CtcpRequest request = new CtcpRequest(rawLine,connection);
			for(CtcpRequestListener listener : connection.getDelegates(CtcpRequestListener.class)) {
				  listener.onCtcpRequest(request);
			}
		}
	}
}
