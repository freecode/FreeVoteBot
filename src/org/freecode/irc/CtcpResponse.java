package org.freecode.irc;

import static org.freecode.irc.CtcpRequest.CTCP;

/**
 * User: Shivam
 * Date: 28/07/13
 * Time: 21:48
 */
public class CtcpResponse extends Notice {

	private final String command;
	private final String response;

	public CtcpResponse(String rawLine, IrcConnection connection) {
		super(rawLine, connection);
		String message = getMessage();
		if (message.charAt(0) == CTCP && message.charAt(message.length() - 1) == CTCP) {
			message = message.replace(String.valueOf(CTCP), "").trim();
			String[] sstr = message.split(" ", 2);
			if (sstr.length > 0) {
				command = sstr[0];
				response = sstr.length > 1 ? sstr[1] : null;
			} else {
				throw new IllegalArgumentException("Not a valid CTCP response");
			}
		} else {
			throw new IllegalArgumentException("Not a valid CTCP response");
		}
	}

	public CtcpResponse(IrcConnection connection, final String target, final String command, final String response) {
		super(target, CTCP + command + " " + response + CTCP, connection);
		this.command = command;
		this.response = response;
	}
	public String getCommand() {
		return command;
	}

	public String getResponse() {
		return response;
	}


	public static boolean isCtcpResponse(final String raw) {
		String[] parts = raw.split(" ", 4);
		if (parts.length == 4 && parts[1].equalsIgnoreCase("NOTICE")) {
			String msg = parts[3];
			if (msg.startsWith(":")) {
				msg = msg.substring(1);
			}
			return msg.charAt(0) == CTCP && msg.charAt(msg.length() - 1) == CTCP;
		}
		return false;
	}
}
