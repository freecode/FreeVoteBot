package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.Notice;
import org.freecode.irc.PrivateMsg;
import org.freecode.irc.votebot.api.CommandModule;
import org.freecode.irc.votebot.dao.PollDAO;
import org.freecode.irc.votebot.dao.VoteDAO;
import org.freecode.irc.votebot.entity.Poll;
import org.freecode.irc.votebot.entity.Vote;

import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class PollsModule extends CommandModule {
    private PollDAO pollDAO;
    private VoteDAO voteDAO;

    @Override
    public void processMessage(PrivateMsg privateMsg) {
        try {
            Poll[] polls = pollDAO.getOpenPolls();
            privateMsg.getIrcConnection().send(new Notice(privateMsg.getNick(), "List of polls:", privateMsg.getIrcConnection()));

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
                privateMsg.getIrcConnection().send(new Notice(privateMsg.getNick(), msg, privateMsg.getIrcConnection()));
            }
            privateMsg.getIrcConnection().send(new Notice(privateMsg.getNick(), "End list of polls.", privateMsg.getIrcConnection()));
        } catch (SQLException e) {
            privateMsg.send(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "polls";
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
