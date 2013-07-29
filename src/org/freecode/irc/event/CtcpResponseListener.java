package org.freecode.irc.event;

import org.freecode.irc.CtcpResponse;
import org.freecode.irc.event.internal.DelegateListener;

/**
 * User: Shivam
 * Date: 29/07/13
 * Time: 15:57
 */
public interface CtcpResponseListener extends DelegateListener {
	public void onCtcpResponse(CtcpResponse response);
}
