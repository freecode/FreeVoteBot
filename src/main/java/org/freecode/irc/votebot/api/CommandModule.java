package org.freecode.irc.votebot.api;

import org.freecode.irc.Privmsg;
import org.freecode.irc.Transmittable;

public abstract class CommandModule extends FVBModule {
    @Override
    public boolean canRun(Transmittable trns) {
        if (!trns.isPrivmsg())
            return false;
        String msg = trns.asPrivmsg().getMessage();
        String command = "!" + getName().toLowerCase();
        return msg.toLowerCase().startsWith(command)
                && msg.substring(command.length()).matches(getParameterRegex());
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
