package org.freecode.irc.votebot.api;

import com.speed.irc.connection.Server;
import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.FreeVoteBot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CommandModule extends FVBModule {
	private final Pattern COMMAND_PATTERN;
	private final Pattern NAME_PATTERN, PARAMETER_PATTERN;

	public CommandModule() {
		char c = getCommandCharacter();
		this.COMMAND_PATTERN = Pattern.compile(String.format("(%s([^ ]+))|(%s(.+?) (.+))", c, c));
		this.PARAMETER_PATTERN = Pattern.compile(getParameterRegex());
		this.NAME_PATTERN = Pattern.compile(getName());
	}

	@Override
	public boolean canRun(Privmsg trns) {
		String msg = trns.getMessage();
		Matcher matcher = COMMAND_PATTERN.matcher(msg);
		if (matcher.matches()) {
			if (matcher.group(4) == null || matcher.group(4).isEmpty()) {
				return NAME_PATTERN.matcher(matcher.group(2)).matches();
			} else {
				return NAME_PATTERN.matcher(matcher.group(4)).matches() &&
						PARAMETER_PATTERN.matcher(matcher.group(5)).matches();
			}
		}
		return false;
	}

	@Override
	public void process(Privmsg trns) {
		processMessage(trns);
	}

	protected void askChanServForUserCreds(Privmsg privmsg) {
		Server server = privmsg.getConversable().getServer();
		server.sendMessage(new Privmsg("WHY " + FreeVoteBot.CHANNEL_SOURCE + " "
				+ privmsg.getSender(), null, server.getUser("ChanServ")));
	}

	public abstract void processMessage(Privmsg privmsg);

	protected String getParameterRegex() {
		return ".*";
	}

	protected char getCommandCharacter() {
		return '!';
	}
}
