package org.freecode.irc.votebot.api;

import org.freecode.irc.Privmsg;
import org.freecode.irc.Transmittable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CommandModule extends FVBModule {

	private static final Pattern COMMAND_PATTERN = Pattern.compile("(!([^ ]+))|(!(.+?) (.+))");

	@Override
	public boolean canRun(Transmittable trns) {
		if (!trns.isPrivmsg())
			return false;
		String msg = trns.asPrivmsg().getMessage();
		Matcher matcher = COMMAND_PATTERN.matcher(msg);
		if (matcher.matches()) {
			int count = matcher.groupCount();
			if (matcher.group(4) == null || matcher.group(4).isEmpty()) {
				return matcher.group(2).matches(getName());
			} else {
                return matcher.group(4).matches(getName()) && matcher.group(5).matches(getParameterRegex());
			}
		}
		return false;
	}

	@Override
	public void process(Transmittable trns) {
		processMessage((Privmsg) trns);
	}

	public abstract void processMessage(Privmsg privmsg);

	protected String getParameterRegex() {
		return ".*";
	}
}
