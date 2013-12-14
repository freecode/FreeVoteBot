package org.freecode.irc.votebot.modules.common;

import org.freecode.irc.Notice;
import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.FreeVoteBot;
import org.freecode.irc.votebot.api.CommandModule;
import org.freecode.irc.votebot.dao.PollDAO;
import org.freecode.irc.votebot.dao.VoteDAO;
import org.freecode.irc.votebot.entity.Poll;
import org.freecode.irc.votebot.entity.Vote;

import java.sql.Date;
import java.sql.SQLException;

public class PollsModule extends CommandModule {
    private PollDAO pollDAO;
    private VoteDAO voteDAO;

    @Override
    public void processMessage(Privmsg privmsg) {
        try {
            Poll[] polls = pollDAO.getOpenPolls();
            privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "List of polls:", privmsg.getIrcConnection()));

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
                        " Ends: " + FreeVoteBot.SDF.format(new Date(poll.getExpiry())) + " Created by: " + poll.getCreator() +
                        " Yes: " + yes + " No: " + no + " Abstain: " + abstain;
                privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), msg, privmsg.getIrcConnection()));
            }
            privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "End list of polls.", privmsg.getIrcConnection()));
        } catch (SQLException e) {
            privmsg.send(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "polls";
    }

    public void setPollDAO(PollDAO pollDAO) {
        this.pollDAO = pollDAO;
    }

    public void setVoteDAO(VoteDAO voteDAO) {
        this.voteDAO = voteDAO;
    }
}
