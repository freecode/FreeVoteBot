package org.freecode.irc.votebot.dao;

import org.freecode.irc.votebot.entity.Poll;
import org.freecode.irc.votebot.entity.Vote;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/21/13
 * Time: 7:32 PM
 */
public class VoteDAO extends JdbcDaoSupport {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS votes (pollId integer, voter string NOT NULL, answerIndex integer NOT NULL)";
    private static final String GET_PREVIOUS_VOTE_OF_USER_ON_POLL = "SELECT * FROM votes WHERE voter = ? AND pollId = ? LIMIT 1";
    private static final String UPDATE_VOTE_OF_USER_ON_POLL = "UPDATE votes SET answerIndex = ? WHERE voter = ? AND pollId = ?";
    private static final String ADD_NEW_VOTE = "INSERT INTO votes(pollId,voter,answerIndex) VALUES (?,?,?)";
    private static final String GET_VOTES_ON_POLL = "SELECT * FROM votes WHERE pollId = ?";

    public void createTable() throws SQLException {
        getJdbcTemplate().execute(CREATE_TABLE);
    }

    public Vote getUsersVoteOnPoll(final String voter, final int pollId) throws SQLException {
        try {
            return getJdbcTemplate().queryForObject(GET_PREVIOUS_VOTE_OF_USER_ON_POLL,
                    new Object[] {voter, pollId},
                    new BeanPropertyRowMapper<>(Vote.class));
        } catch (EmptyResultDataAccessException empty) {
            return null;
        }
    }

    public void updateUsersVote(final Vote vote) throws SQLException {
        getJdbcTemplate().update(UPDATE_VOTE_OF_USER_ON_POLL, vote.getAnswerIndex(), vote.getVoter(), vote.getPollId());
    }

    public void addUsersVote(final String voter, final int pollId, final int answerIndex) throws SQLException {
        getJdbcTemplate().update(ADD_NEW_VOTE, pollId, voter, answerIndex);
    }

    public List<Vote> getVotesOnPoll(final int pollId) throws SQLException {
        try {
            return getJdbcTemplate().query(GET_VOTES_ON_POLL,
                    new Object[]{pollId},
                    new BeanPropertyRowMapper<>(Vote.class));
        } catch (EmptyResultDataAccessException empty) {
            return Collections.emptyList();
        }
    }

    public Poll[] getPollsNotVotedIn(Poll[] polls, String nick) throws SQLException {
        List<Poll> pollsNotVotedIn = new ArrayList<>();
        for (Poll poll : polls) {
            Vote vote = getUsersVoteOnPoll(nick, poll.getId());
            if (vote == null) {
                pollsNotVotedIn.add(poll);
            }
        }
        return pollsNotVotedIn.toArray(new Poll[pollsNotVotedIn.size()]);
    }
}
