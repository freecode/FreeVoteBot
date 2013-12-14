package org.freecode.irc.votebot.api;

import org.freecode.irc.Privmsg;
import org.freecode.irc.Transmittable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CommandModule extends FVBModule {

    private static final Pattern COMMAND_PATTERN = Pattern.compile("(!([^ ]+))|(!(.+?) (.+))");
    private final Pattern NAME_PATTERN, PARAMETER_PATTERN;

    public CommandModule() {
        this.PARAMETER_PATTERN = Pattern.compile(getParameterRegex());
        this.NAME_PATTERN = Pattern.compile(getName());
    }

    @Override
    public boolean canRun(Transmittable trns) {
        if (!trns.isPrivmsg())
            return false;
        String msg = trns.asPrivmsg().getMessage();
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
    public void process(Transmittable trns) {
        processMessage((Privmsg) trns);
    }

    public abstract void processMessage(Privmsg privmsg);

    protected String getParameterRegex() {
        return ".*";
    }
}
