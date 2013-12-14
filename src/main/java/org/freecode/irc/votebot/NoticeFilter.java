package org.freecode.irc.votebot;

import org.freecode.irc.IrcConnection;
import org.freecode.irc.Notice;
import org.freecode.irc.event.NoticeListener;

/**
 * User: Shivam
 * Date: 29/07/13
 * Time: 23:16
 */
public abstract class NoticeFilter implements NoticeListener {


    static class NoticeFilterQueue extends ExpiryQueue<NoticeFilter> {

        private final IrcConnection connection;

        public NoticeFilterQueue(long defaultExpiry, IrcConnection connection) {
            super(defaultExpiry);
            this.connection = connection;
        }

        public void onRemoval(NoticeFilter notice) {
            connection.removeListener(notice);
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

    public static void setFilterQueue(IrcConnection connection, long delay) {
        queue = new NoticeFilterQueue(delay, connection);
    }

    public abstract boolean accept(Notice notice);

    public abstract void run(Notice notice);

    public void onNotice(Notice n) {
        if (accept(n)) {
            run(n);
        }
    }
}
