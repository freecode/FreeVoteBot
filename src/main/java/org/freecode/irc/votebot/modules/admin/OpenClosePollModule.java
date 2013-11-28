package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;
import org.freecode.irc.votebot.dao.PollDAO;

import java.sql.SQLException;

public class OpenClosePollModule extends AdminModule {
    private PollDAO pollDAO;

    @Override
    protected Right[] getRights() {
        return new Right[]{Right.AOP, Right.SOP, Right.FOUNDER};
    }

    @Override
    public void processMessage(Privmsg privmsg) {
        String[] parts = privmsg.getMessage().split(" ", 2);
        int id = Integer.parseInt(parts[1]);
        boolean state = false;
        String action = "opened";
        if (parts[0].charAt(1) == 'c') {
            state = true;
            action = "closed";
        }
        try {
            if (pollDAO.setStatusOfPoll(id, state)) {
                privmsg.send("Poll" + action + ".");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "(open|close)poll";
    }

    @Override
    protected String getParameterRegex() {
        return "\\d+";
    }

    public void setPollDAO(PollDAO pollDAO) {
        this.pollDAO = pollDAO;
    }
}
