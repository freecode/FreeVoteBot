package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;
import org.freecode.irc.votebot.dao.PollDAO;

import java.sql.SQLException;

public class OpenPollModule extends AdminModule {
    private PollDAO pollDAO;

    @Override
    protected Right[] getRights() {
        return new Right[]{Right.AOP, Right.SOP, Right.FOUNDER};
    }

    @Override
    public void processMessage(Privmsg privmsg) {
        int id = Integer.parseInt(privmsg.getMessage().split(" ", 2)[1]);
        try {
            if (pollDAO.setStatusOfPoll(id, false)) {
                privmsg.send("Poll opened.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "openpoll";
    }

    @Override
    protected String getParameterRegex() {
        return "\\d+";
    }

    public void setPollDAO(PollDAO pollDAO) {
        this.pollDAO = pollDAO;
    }
}
