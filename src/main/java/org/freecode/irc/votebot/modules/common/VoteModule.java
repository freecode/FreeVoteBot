package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.Notice;
import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.NoticeFilter;
import org.freecode.irc.votebot.api.CommandModule;
import org.freecode.irc.votebot.dao.PollDAO;
import org.freecode.irc.votebot.dao.VoteDAO;
import org.freecode.irc.votebot.entity.Poll;
import org.freecode.irc.votebot.entity.Vote;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Processes and validates voting commands sent by users
 *
 * @author Shivam Mistry
 */
public class VoteModule extends CommandModule {

    @Autowired
    private PollDAO pollDAO;

    @Autowired
    private VoteDAO voteDAO;

    public void processMessage(Privmsg privmsg) {
        String message = privmsg.getMessage();
        if (message.startsWith("!v ") || message.startsWith("!vote ")) {
            final String msg = privmsg.getMessage().substring(privmsg.getMessage().indexOf(' ')).trim();
            System.out.println(msg);
            final String[] split = msg.split(" ", 2);
            if (split.length == 2) {
                String ids = split[0];
                String vote = split[1].toLowerCase();
                final int nId;
                switch (vote) {
                    case "yes":
                        nId = 0;
                        break;
                    case "no":
                        nId = 1;
                        break;
                    case "abstain":
                        nId = 2;
                        break;
                    default:
                        return;
                }
                if (!ids.matches("\\d+")) {
                    return;
                }
                final int id = Integer.parseInt(ids);
                vote(nId, id, privmsg);
            } else if (split.length == 1) {
                String id = split[0];
                if (!id.matches("\\d+")) {
                    return;
                }
                try {

                    int pollId = Integer.parseInt(id);
                    Poll poll = pollDAO.getPoll(pollId);

                    if (poll != null) {
                        String expiry = getDateFormatter().format(new Date(poll.getExpiry()));
                        String closed = poll.isClosed() ? "Closed" : "Open";
                        if (System.currentTimeMillis() >= poll.getExpiry()) {
                            closed = "Expired";
                        }

                        List<Vote> votes = voteDAO.getVotesOnPoll(poll.getId());
                        int yesCount = Collections.frequency(votes, Vote.YES);
                        int noCount = Collections.frequency(votes, Vote.NO);
                        int abstainCount = Collections.frequency(votes, Vote.ABSTAIN);

                        boolean open = closed.equals("Open");
                        privmsg.send("Poll #" + poll.getId() + ": " + poll.getQuestion() +
                                " Created by: " + poll.getCreator() +
                                " Yes: " + yesCount + " No: " + noCount + " Abstain: " + abstainCount +
                                " Status: \u00030" + (open ? "3" : "4") + closed + "\u0003" +
                                (open ? " Ends: " : " Ended: ") + expiry);

                    }
                } catch (SQLException e) {
                    privmsg.send(e.getMessage());
                }
            }

        } else if (message.startsWith("!y ")) {
            String id = message.replace("!y", "").trim();
            if (id.matches("\\d+")) {
                voteYes(Integer.parseInt(id), privmsg);
            }
        } else if (message.startsWith("!n ")) {
            String id = message.replace("!n", "").trim();
            if (id.matches("\\d+")) {
                voteNo(Integer.parseInt(id), privmsg);
            }
        } else if (message.startsWith("!a ")) {
            String id = message.replace("!a", "").trim();
            if (id.matches("\\d+")) {
                voteAbstain(Integer.parseInt(id), privmsg);
            }
        }
    }

    private void voteYes(final int pollId, final Privmsg privmsg) {
        vote(0, pollId, privmsg);
    }

    private void voteNo(final int pollId, final Privmsg privmsg) {
        vote(1, pollId, privmsg);
    }

    private void voteAbstain(final int pollId, final Privmsg privmsg) {
        vote(2, pollId, privmsg);
    }

    private void vote(final int answerIndex, final int pollId, final Privmsg privmsg) {

        privmsg.getIrcConnection().addListener(new NoticeFilter() {
            public boolean accept(Notice notice) {
                if (notice.getNick().equals("ChanServ") && notice.getMessage().equals("Permission denied.")) {
                    notice.getIrcConnection().removeListener(this);
                    return false;
                }
                return notice.getNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains(privmsg.getNick());
            }

            public void run(Notice notice) {
                try {
                    String mainNick = notice.getMessage().substring(notice.getMessage().indexOf("Main nick:") + 10).trim();
                    System.out.println(mainNick);

                    Poll poll = pollDAO.getPoll(pollId);
                    if (poll != null) {
                        long time = poll.getExpiry();
                        if (System.currentTimeMillis() < time && !poll.isClosed()) {
                            Vote vote = voteDAO.getUsersVoteOnPoll(mainNick, pollId);
                            if (vote != null) {
                                if (vote.getAnswerIndex() == answerIndex) {
                                    privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "You've already voted with this option!", privmsg.getIrcConnection()));
                                } else {
                                    vote.setAnswerIndex(answerIndex);
                                    voteDAO.updateUsersVote(vote);
                                    privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "Vote updated.", privmsg.getIrcConnection()));
                                }
                            } else {
                                voteDAO.addUsersVote(mainNick, pollId, answerIndex);
                                privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "Vote cast.", privmsg.getIrcConnection()));
                            }
                        } else {
                            privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "Voting is closed for this poll.", privmsg.getIrcConnection()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                privmsg.getIrcConnection().removeListener(this);
            }
        });

        askChanServForUserCreds(privmsg);
    }

    private DateFormat getDateFormatter() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        return dateFormat;
    }

    public String getName() {
        return "(vote|v|y|n|a)";
    }
}
