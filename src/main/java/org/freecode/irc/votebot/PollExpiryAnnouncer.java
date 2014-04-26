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
    private boolean hasAnnounced, hasFinished;
    private FreeVoteBot fvb;
    private ScheduledFuture<?> future;

    public PollExpiryAnnouncer(final long expiry, final int id, final FreeVoteBot fvb) {
        this.expiry = expiry;
        this.id = id;
        this.fvb = fvb;
    }

    public void run() {
        long ttl = expiry - System.currentTimeMillis();
        if (!hasAnnounced && ttl <= 300000 && ttl >= 0) { //5 minutes
            hasAnnounced = true;
            fvb.sendMsg(String.format("Poll #%d has less than 5 minutes remaining!", id));
        } else if (ttl <= 0 && !hasFinished) {
            fvb.sendMsg(String.format("Voting for poll #%d has now closed!", id));
            hasFinished = true;
            try {
                Poll poll = fvb.getPollDAO().getPoll(id);
                Vote[] votes = fvb.getVoteDAO().getVotesOnPoll(id);
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
                fvb.sendMsg(String.format("Question: \"%s\", Yes: %d, No: %d, Abstain: %d", poll.getQuestion(), yes, no, abstain));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            getFuture().cancel(true);
        }
    }

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    public ScheduledFuture<?> getFuture() {
        return future;
    }
}
