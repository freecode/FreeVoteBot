package org.freecode.irc.votebot.modules.common;

import com.speed.irc.connection.Server;
import com.speed.irc.types.Notice;
import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.api.CommandModule;
import org.freecode.irc.votebot.dao.PollDAO;
import org.freecode.irc.votebot.dao.VoteDAO;
import org.freecode.irc.votebot.entity.Poll;
import org.freecode.irc.votebot.entity.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

@Component
public class PollsModule extends CommandModule {

    @Autowired
    private PollDAO pollDAO;

    @Autowired
    private VoteDAO voteDAO;

    @Override
    public void processMessage(Privmsg privmsg) {
        try {
            Poll[] polls = pollDAO.getOpenPolls();
            String[] params = privmsg.getMessage().split(" ");
            Server server = privmsg.getConversable().getServer();

            if (params.length != 1) {
                polls = voteDAO.getPollsNotVotedIn(polls, privmsg.getSender());
            }

            if (polls.length == 0) {
                String message = params.length == 1 ? "No active polls to view!" : "No polls to vote in!";
                server.sendNotice(new Notice(message, null, privmsg.getSender(), server));
                return;
            }

            String message = params.length == 1 ? "List of polls:" : "List of polls not voted in:";
            server.sendNotice(new Notice(message, null, privmsg.getSender(), server));

            for (Poll poll : polls) {
                Vote[] votes = voteDAO.getVotesOnPoll(poll.getId());
                int yes = 0, no = 0, abstain = 0;
                for (Vote vote : votes) {
                    int i = vote.getAnswerIndex();
                    if (i == 0) {
                        yes++;
                    } else if (i == 1) {
                        no++;
                    } else if (i == 2) {
                        abstain++;
                    }
                }

                System.out.println(poll.getExpiry());
                String msg = "Poll #" + poll.getId() + ": " + poll.getQuestion() +
                        " Ends: " + getDateFormatter().format(new Date(poll.getExpiry())) + " Created by: " + poll.getCreator() +
                        " Yes: " + yes + " No: " + no + " Abstain: " + abstain;
                server.sendNotice(new Notice(msg, null, privmsg.getSender(), server));
            }
            server.sendNotice(new Notice("End list of polls", null, privmsg.getSender(), server));
        } catch (SQLException e) {
            privmsg.getConversable().sendMessage(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "polls";
    }

    @Override
    protected String getParameterRegex() {
        return "(notvoted)?";
    }

    private DateFormat getDateFormatter() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        return dateFormat;
    }

    public void setPollDAO(PollDAO pollDAO) {
        this.pollDAO = pollDAO;
    }

    public void setVoteDAO(VoteDAO voteDAO) {
        this.voteDAO = voteDAO;
    }
}
