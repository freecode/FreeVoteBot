package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.Notice;
import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.CommandModule;
import org.freecode.irc.votebot.dao.PollDAO;
import org.freecode.irc.votebot.dao.VoteDAO;
import org.freecode.irc.votebot.entity.Poll;
import org.freecode.irc.votebot.entity.Vote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PollsModule extends CommandModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollsModule.class);

    @Autowired
    private PollDAO pollDAO;
    @Autowired
    private VoteDAO voteDAO;

    @Override
    public void processMessage(Privmsg privmsg) {
        try {
            Poll[] polls = pollDAO.getOpenPolls();
            String[] params = privmsg.getMessage().split(" ");

            if (params.length != 1) {
                polls = voteDAO.getPollsNotVotedIn(polls, privmsg.getNick());
            }

            if (polls.length == 0) {
                String message = params.length == 1 ? "No active polls to view!" : "No polls to vote in!";
                privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), message, privmsg.getIrcConnection()));
                return;
            }

            String message = params.length == 1 ? "List of polls:" : "List of polls not voted in:";
            privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), message, privmsg.getIrcConnection()));

            for (Poll poll : polls) {
                List<Vote> votes = voteDAO.getVotesOnPoll(poll.getId());
                int yesCount = Collections.frequency(votes, Vote.YES);
                int noCount = Collections.frequency(votes, Vote.NO);
                int abstainCount = Collections.frequency(votes, Vote.ABSTAIN);

                System.out.println(poll.getExpiry());
                String msg = "Poll #" + poll.getId() + ": " + poll.getQuestion() +
                        " Ends: " + getDateFormatter().format(new Date(poll.getExpiry())) + " Created by: " + poll.getCreator() +
                        " Yes: " + yesCount + " No: " + noCount + " Abstain: " + abstainCount;
                privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), msg, privmsg.getIrcConnection()));
            }

            privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "End list of polls.", privmsg.getIrcConnection()));
        } catch (SQLException e) {
            LOGGER.error("Failed to open poll.", e);
            privmsg.send(e.getMessage());
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
}
