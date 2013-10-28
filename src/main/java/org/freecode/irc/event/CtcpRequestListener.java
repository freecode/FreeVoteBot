package org.freecode.irc.event;

import org.freecode.irc.CtcpRequest;
import org.freecode.irc.event.internal.DelegateListener;

/**
 * User: Shivam
 * Date: 28/07/13
 * Time: 20:44
 */
public interface CtcpRequestListener extends DelegateListener {

	public void onCtcpRequest(CtcpRequest request);
}
