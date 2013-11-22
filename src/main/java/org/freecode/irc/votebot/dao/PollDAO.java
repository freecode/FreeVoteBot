package org.freecode.irc.votebot.dao;

import org.freecode.irc.votebot.entity.Poll;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/21/13
 * Time: 7:33 PM
 */
public class PollDAO extends AbstractDAO {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS polls (id integer PRIMARY KEY AUTOINCREMENT, question string NOT NULL, options string NOT NULL DEFAULT 'yes,no,abstain', closed BOOLEAN DEFAULT 0, expiry INTEGER DEFAULT 0, creator STRING DEFAULT 'null')";
    private static final String GET_OPEN_POLL_BY_ID = "SELECT * FROM polls WHERE id = ? AND closed = 0 LIMIT 1";
    private static final String GET_POLL_BY_ID = "SELECT * FROM polls WHERE id = ? LIMIT 1";
    private static final String GET_OPEN_POLLS_THAT_EXPIRED = "SELECT * FROM polls WHERE closed = 0 AND expiry > ?";
    private static final String SET_POLL_STATUS_BY_ID = "UPDATE polls SET closed = ? WHERE id = ?";
    private static final String ADD_NEW_POLL = "INSERT INTO polls(question, expiry, creator) VALUES (?,?,?)";

    private ResultSet resultSet;
    private PreparedStatement statement;

    public void createTable() throws SQLException {
        statement = dbConn.prepareStatement(CREATE_TABLE);
        statement.setQueryTimeout(5);
        statement.execute();
    }

    public Poll getOpenPoll(int id) throws SQLException {
        statement = dbConn.prepareStatement(GET_OPEN_POLL_BY_ID);
        statement.setInt(1, id);
        resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return new Poll(resultSet);
        }
        return null;
    }

    public int addNewPoll(final String question, final long expiry, final String creator) throws SQLException {
        PreparedStatement statement = dbConn.prepareStatement(ADD_NEW_POLL, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, question);
        statement.setLong(2, expiry);
        statement.setString(3, creator);
        statement.execute();
        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }

        else return -1;
    }

    public Poll getPoll(int id) throws SQLException {
        statement = dbConn.prepareStatement(GET_POLL_BY_ID);
        statement.setInt(1, id);
        resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return new Poll(resultSet);
        }
        return null;
    }

    public Poll[] getOpenPolls() throws SQLException {
        final long currentTime = System.currentTimeMillis();
        final List<Poll> polls = new ArrayList<>();
        statement = dbConn.prepareStatement(GET_OPEN_POLLS_THAT_EXPIRED);
        statement.setLong(1, currentTime);
        resultSet = statement.executeQuery();

        while (resultSet.next()) {
            polls.add(new Poll(resultSet));
        }

        return polls.toArray(new Poll[polls.size()]);
    }

    public boolean setStatusOfPoll(final int id, final boolean status) throws SQLException {
        statement = dbConn.prepareStatement(SET_POLL_STATUS_BY_ID);
        statement.setBoolean(1, status);
        statement.setInt(2, id);
        return statement.executeUpdate() > 0;
    }
}
