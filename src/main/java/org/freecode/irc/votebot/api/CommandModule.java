package org.freecode.irc.votebot.api;

import org.freecode.irc.PrivateMsg;
import org.freecode.irc.Transmittable;
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
    public boolean canRun(Transmittable trns) {
        if (!trns.isPrivateMsg())
            return false;
        String msg = trns.asPrivateMsg().getMessage();
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
        processMessage((PrivateMsg) trns);
    }

    protected void askChanServForUserCreds(PrivateMsg privateMsg) {
        privateMsg.getIrcConnection().send(new PrivateMsg("ChanServ", "WHY " + FreeVoteBot.CHANNEL_SOURCE + " " + privateMsg.getNick(), privateMsg.getIrcConnection()));
    }

    public abstract void processMessage(PrivateMsg privateMsg);

    protected String getParameterRegex() {
        return ".*";
    }

    protected char getCommandCharacter() {
        return '!';
    }
}
