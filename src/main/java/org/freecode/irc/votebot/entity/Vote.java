package org.freecode.irc.votebot.entity;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/21/13
 * Time: 7:29 PM
 */
public class Vote {
    private int pollId, answerIndex;
    public static final int YES = 0;
    public static final int NO = 1;
    public static final int ABSTAIN = 2;
    private String voter;

    public int getPollId() {
        return pollId;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }

    public String getVoter() {
        return voter;
    }

    public void setAnswerIndex(int answerIndex) {
        this.answerIndex = answerIndex;
    }

    public void setPollId(int pollId) {
        this.pollId = pollId;
    }

    public void setVoter(String voter) {
        this.voter = voter;
    }
}
