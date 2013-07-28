package org.freecode.irc.event;

import org.freecode.irc.Privmsg;
import org.freecode.irc.event.internal.DelegateListener;

/**
 * User: Shivam
 * Date: 17/06/13
 * Time: 01:02
 */
public interface PrivateMessageListener extends DelegateListener {
    public void onPrivmsg(Privmsg privmsg);
}
