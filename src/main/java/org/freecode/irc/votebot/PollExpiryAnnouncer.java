package org.freecode.irc.votebot;

import org.freecode.irc.votebot.entity.Poll;
import org.freecode.irc.votebot.entity.Vote;

import java.sql.SQLException;
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
        String text = "Poll #%d (\"%s\") has less than %s remaining!";

        try {
            Poll poll = fvb.getPollDAO().getPoll(id);
            String question = poll.getQuestion();

            if ((hasAnnounced & 1) == 0 && ttl <= 2 * 300000 && ttl >= 0) {
                hasAnnounced |= 1;
                fvb.sendMsg(String.format(text, id, question, "10 minutes"));
            } else if ((hasAnnounced & 3) == 0 && ttl <= MILLIS_IN_AN_HOUR * 2 && ttl >= 0) {
                hasAnnounced |= 2;
                fvb.sendMsg(String.format(text, id, question, "two hours"));
            } else if ((hasAnnounced & 7) == 0 && ttl <= 12 * MILLIS_IN_AN_HOUR && ttl >= 0) {
                hasAnnounced |= 4;
                fvb.sendMsg(String.format(text, id, question, "twelve hours"));
            } else if (ttl <= 0 && ((hasAnnounced & Integer.MAX_VALUE) != Integer.MAX_VALUE)) {
                hasAnnounced = Integer.MAX_VALUE;
                getFuture().cancel(true);
                Vote[] votes = fvb.getVoteDAO().getVotesOnPoll(id);
                int total = votes.length;
                int yes = 0, no = 0, abstain = 0;
                for (Vote v : votes) {
                    switch (v.getAnswerIndex()) {
                        case Vote.YES:
                            yes++;
                            break;
                        case Vote.NO:
                            no++;
                            break;
                        case Vote.ABSTAIN:
                            abstain++;
                            break;
                        default:
                            break;
                    }
                }
                String result = (total >= 5 && yes > no && yes > abstain) ? "passed" : "did not pass";
                fvb.sendMsg(String.format("Poll #%d %s!", id, result));
                fvb.sendMsg(String.format("Question: \"%s\", Yes: %d, No: %d, Abstain: %d", question, yes, no, abstain));
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
