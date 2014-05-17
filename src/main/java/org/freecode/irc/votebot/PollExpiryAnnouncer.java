package org.freecode.irc.votebot;

import org.freecode.irc.votebot.entity.Poll;
import org.freecode.irc.votebot.entity.Vote;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by shivam on 22/04/14.
 */
public class PollExpiryAnnouncer implements Runnable {

    private final long expiry;
    private final int id;
    private int hasAnnounced = 0;
    private FreeVoteBot fvb;
    private ScheduledFuture<?> future;
    private static long MILLIS_IN_AN_HOUR = 3600000L;

    public PollExpiryAnnouncer(final long expiry, final int id, final FreeVoteBot fvb) {
        this.expiry = expiry;
        this.id = id;
        this.fvb = fvb;
    }

    public void run() {
        long ttl = expiry - System.currentTimeMillis();
        String time_announcement = "Poll #%d has less than %s remaining!";
        String poll_information = "Question: \"%s\", Yes: %d, No: %d, Abstain: %d";

        try {
            Poll poll = fvb.getPollDAO().getPoll(id);
            String question = poll.getQuestion();

            List<Vote> votes = fvb.getVoteDAO().getVotesOnPoll(poll.getId());
            int total = votes.size();
            int yesCount = Collections.frequency(votes, Vote.YES);
            int noCount = Collections.frequency(votes, Vote.NO);
            int abstainCount = Collections.frequency(votes, Vote.ABSTAIN);

            if ((hasAnnounced & 1) == 0 && ttl <= 2 * 300000 && ttl >= 0) {
                hasAnnounced |= 1;
                fvb.sendMsg(String.format(time_announcement, id, "10 minutes"));
                fvb.sendMsg(String.format(poll_information, question, yesCount, noCount, abstainCount));
            } else if ((hasAnnounced & 3) == 0 && ttl <= MILLIS_IN_AN_HOUR * 2 && ttl >= 0) {
                hasAnnounced |= 2;
                fvb.sendMsg(String.format(time_announcement, id, "two hours"));
                fvb.sendMsg(String.format(poll_information, question, yesCount, noCount, abstainCount));
            } else if ((hasAnnounced & 7) == 0 && ttl <= 12 * MILLIS_IN_AN_HOUR && ttl >= 0) {
                hasAnnounced |= 4;
                fvb.sendMsg(String.format(time_announcement, id, "twelve hours"));
                fvb.sendMsg(String.format(poll_information, question, yesCount, noCount, abstainCount));
            } else if (ttl <= 0 && ((hasAnnounced & Integer.MAX_VALUE) != Integer.MAX_VALUE)) {
                hasAnnounced = Integer.MAX_VALUE;
                String result = (total >= 5 && yesCount > noCount && yesCount > abstainCount) ? "passed" : "did not pass";
                fvb.sendMsg(String.format("Poll #%d %s!", id, result));
                fvb.sendMsg(String.format(poll_information, question, yesCount, noCount, abstainCount));
                getFuture().cancel(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    public ScheduledFuture<?> getFuture() {
        return future;
    }
}
