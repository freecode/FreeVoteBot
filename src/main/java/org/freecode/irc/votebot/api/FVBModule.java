package org.freecode.irc.votebot.api;

import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.FreeVoteBot;

/**
 * Created with IntelliJ IDEA.
 * User: shivam
 * Date: 10/28/13
 * Time: 6:02 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FVBModule implements Runnable {
    private final FreeVoteBot fvb;
    private volatile boolean enabled = true;

    public FVBModule(final FreeVoteBot fvb) {
        this.fvb = fvb;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean b) {
        enabled = b;
    }

    public abstract boolean canRun(final Transmittable trns);

    public abstract boolean process(final Transmittable trns);

    public abstract String getName();


    public String toString() {
        return getName();
    }

    public void run() {

    }

    public FreeVoteBot getFvb() {
        return fvb;
    }
}
