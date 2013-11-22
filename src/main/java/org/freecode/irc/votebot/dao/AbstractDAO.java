package org.freecode.irc.votebot.dao;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/21/13
 * Time: 11:22 PM
 */
public abstract class AbstractDAO {
    protected Connection dbConn;

    public void setDBConnection(BasicDataSource dataSource) throws SQLException {
        this.dbConn = dataSource.getConnection();
    }
}
