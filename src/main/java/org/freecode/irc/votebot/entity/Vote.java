package org.freecode.irc.votebot.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/21/13
 * Time: 7:29 PM
 */
public class Vote {
    private int pollId, answerIndex;
    private String voter;

    public Vote(ResultSet rs) throws SQLException {
        pollId = rs.getInt(1);
        voter = rs.getString(2);
        answerIndex = rs.getInt(3);
    }

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
}
