package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.Notice;
import org.freecode.irc.Privmsg;
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
    public void processMessage(Privmsg privmsg) {
        try {
            Poll[] polls = getRelevantPolls(privmsg);
            String[] params = privmsg.getMessage().split(" ");

            if (polls.length == 0) {
                String message = "";
                switch (params.length) {
                    case 1:
                        message = "No active polls to view!";
                    case 2:
                        message = "No polls to vote in!";
                    default:
                        message = "No results!";
                }
                privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), message, privmsg.getIrcConnection()));
                return;
            }

            String message = "";
            switch (params.length) {
                case 1:
                    message = "List of polls:";
                case 2:
                    message = "List of polls not voted in:";
                default:
                    message = "List of polls containing \"" + message.substring(message.indexOf("search") + 7) + "\":";
            }
            privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), message, privmsg.getIrcConnection()));

            if (params.length <= 2) {
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
                    privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), msg, privmsg.getIrcConnection()));
                }
                privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "End list of polls.", privmsg.getIrcConnection()));
            } else {
                StringBuilder results = new StringBuilder();
                for (int i = 0; i < polls.length; i++) {
                    results.append("#");
                    results.append(polls[i].getId());
                    if(i != polls.length - 1){
                        results.append(", ");
                    }
                }
                privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), results.toString(), privmsg.getIrcConnection()));
                privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "End results of search.", privmsg.getIrcConnection()));
            }
        } catch (SQLException e) {
            privmsg.send(e.getMessage());
        }
    }

    private Poll[] getRelevantPolls(Privmsg privmsg) throws SQLException {
        String message = privmsg.getMessage();
        String[] params = message.split(" ");
        switch (params.length) {
            case 1:
                return pollDAO.getOpenPolls();
            case 2:
                return voteDAO.getPollsNotVotedIn(pollDAO.getOpenPolls(), privmsg.getNick());
            default:
                return pollDAO.getPollsContaining(message.substring(message.indexOf("search") + 7));
        }
    }

    @Override
    public String getName() {
        return "polls";
    }

    @Override
    protected String getParameterRegex() {
        return "(notvoted|(search\\s.+))?";
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
