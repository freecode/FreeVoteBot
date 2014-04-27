package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.PollExpiryAnnouncer;
import org.freecode.irc.votebot.api.AdminModule;
import org.freecode.irc.votebot.dao.PollDAO;
import org.freecode.irc.votebot.entity.Poll;

import java.sql.SQLException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
            if (pollDAO.setStatusOfPoll(id, state) > 0) {
                privmsg.send("Poll " + action + ", faggot.");
                if (action.equalsIgnoreCase("closed")) {
                    Future future = getFvb().pollFutures.get(id);
                    if (future != null) {
                        future.cancel(true);
                    }
                } else if (action.equalsIgnoreCase("opened")) {
                    Future future = getFvb().pollFutures.get(id);
                    if (future != null) {
                        future.cancel(true);
                    }
                    Poll poll = pollDAO.getPoll(id);
                    if (poll.getExpiry() > System.currentTimeMillis()) {
                        PollExpiryAnnouncer announcer = new PollExpiryAnnouncer(poll.getExpiry(), poll.getId(), getFvb());
                        ScheduledFuture f = getFvb().pollExecutor.scheduleAtFixedRate(announcer, 5000L, 500L, TimeUnit.MILLISECONDS);
                        getFvb().pollFutures.put(id, f);
                        announcer.setFuture(f);
                    }
                }
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
