package org.freecode.irc.votebot;

import org.freecode.irc.Notice;
import org.freecode.irc.event.NoticeListener;

/**
 * User: Shivam
 * Date: 29/07/13
 * Time: 23:16
 */
public abstract class NoticeFilter implements NoticeListener {
	public abstract boolean accept(Notice notice);

	public abstract void run(Notice notice);

	public void onNotice(Notice n) {
		if (accept(n)) {
			run(n);
		}
	}
}
