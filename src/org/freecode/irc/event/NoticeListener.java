package org.freecode.irc.event;

import org.freecode.irc.Notice;
import org.freecode.irc.event.internal.DelegateListener;

/**
 * User: Shivam
 * Date: 29/07/13
 * Time: 15:56
 */
public interface NoticeListener extends DelegateListener {
	public void onNotice(Notice n);
}
