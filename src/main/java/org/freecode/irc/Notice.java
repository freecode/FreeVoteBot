package org.freecode.irc;

/**
 * User: Shivam
 * Date: 28/07/13
 * Time: 21:42
 */
public class Notice extends Transmittable {

	private final String target;
	private final String message;
	private final String senderMask;
	private final String user;
	private final String nick;
	private final String host;
	private final IrcConnection connection;

	public Notice(String rawLine, IrcConnection connection) {
		this.connection = connection;
		final String[] parts = rawLine.split(" ", 4);
		senderMask = parts[0];
		if(senderMask.contains("!")) {
			nick = senderMask.substring(0, senderMask.indexOf('!'));
			user = senderMask.substring(senderMask.indexOf('!') + 1, senderMask.indexOf('@'));
			host = senderMask.substring(senderMask.indexOf('@') + 1);
		} else {
			nick = senderMask;
			user = null;
			host = null;
		}
		message = parts[3].substring(1);
		target = parts[2];
	}

	public Notice(final String target, final String message, final IrcConnection connection) {
		this.connection = connection;
		this.message = message;
		this.target = target;
		this.senderMask = null;
		this.user = null;
		this.nick = null;
		this.host = null;
	}
	public String getTarget() {
		return target;
	}

	public String getMessage() {
		return message;
	}

	public String getSenderMask() {
		return senderMask;
	}

	public String getUser() {
		return user;
	}

	public String getNick() {
		return nick;
	}

	public String getHost() {
		return host;
	}

	public IrcConnection getIrcConnection() {
		return connection;
	}

	public String getRaw() {
		return String.format("NOTICE %s :%s", target, message);
	}

	public String getCommand() {
		return "NOTICE";
	}
}
