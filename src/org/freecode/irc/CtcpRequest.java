package org.freecode.irc;

/**
 * User: Shivam
 * Date: 25/07/13
 * Time: 14:23
 */
public class CtcpRequest extends Privmsg {
	public static final char CTCP = '\u0001';
	private final String command;
	private final String arguments;

	public CtcpRequest(String rawLine, IrcConnection connection) {
		super(rawLine, connection);
		String message = getMessage();
		if(message.charAt(0) == CTCP && message.charAt(message.length() - 1) == CTCP) {
			message = message.replace(String.valueOf(CTCP), "").trim();
			String[] sstr = message.split(" ",2);
			if(sstr.length > 0) {
				command = sstr[0];
				arguments = sstr.length > 1 ? sstr[1] : null;
			} else {
				throw new IllegalArgumentException("Not a valid CTCP request");
			}
		} else {
			throw new IllegalArgumentException("Not a valid CTCP request");
		}
	}

	public String getCommand() {
		return command;
	}

	public String getArguments() {
		return arguments;
	}

	public static boolean isCtcpRequest(final String raw) {
		String[] parts = raw.split(" ", 4);
		if(parts.length == 4 && parts[1].equalsIgnoreCase("PRIVMSG")) {
			 String msg = parts[3];
			if(msg.startsWith(":")) {
				msg = msg.substring(1);
			}
			return msg.charAt(0) == CTCP && msg.charAt(msg.length() - 1) == CTCP;
		}
		return false;
	}
}
