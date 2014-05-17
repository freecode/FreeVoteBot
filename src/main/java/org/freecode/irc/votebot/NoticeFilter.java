package org.freecode.irc.votebot;

import com.speed.irc.connection.Server;
import com.speed.irc.event.message.NoticeEvent;
import com.speed.irc.event.message.NoticeListener;
import com.speed.irc.types.Notice;

/**
 * User: Shivam
 * Date: 29/07/13
 * Time: 23:16
 */
public abstract class NoticeFilter implements NoticeListener {


    static class NoticeFilterQueue extends ExpiryQueue<NoticeFilter> {

        private final Server connection;

        public NoticeFilterQueue(long defaultExpiry, Server connection) {
            super(defaultExpiry);
            this.connection = connection;
        }

        public void onRemoval(NoticeFilter notice) {
            connection.getEventManager().removeListener(notice);
        }
    }

    private static NoticeFilterQueue queue;


    public NoticeFilter(boolean insertIntoQueue) {
        if (insertIntoQueue && queue != null) {
            queue.insert(this);
        }
    }

    public NoticeFilter() {
        this(true);
    }

    public static void setFilterQueue(Server connection, long delay) {
        queue = new NoticeFilterQueue(delay, connection);
    }

    public abstract boolean accept(Notice notice);

    public abstract void run(Notice notice);

	@Override
    public void noticeReceived(NoticeEvent n) {
        if (accept(n.getNotice())) {
            run(n.getNotice());
        }
    }
}
