package org.freecode.irc.event;

import org.freecode.irc.event.internal.DelegateListener;

/**
 * Created by shivam on 26/04/14.
 */
public interface JoinListener extends DelegateListener {

    public void onJoin(String channel, String nick, String mask);
}
