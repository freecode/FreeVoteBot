package org.freecode.irc.votebot;

import org.freecode.irc.*;
import org.freecode.irc.event.CtcpRequestListener;
import org.freecode.irc.event.NumericListener;
import org.freecode.irc.event.PrivateMessageListener;
import org.freecode.irc.votebot.api.FVBModule;
import org.freecode.irc.votebot.dao.PollDAO;
import org.freecode.irc.votebot.dao.VoteDAO;
import org.freecode.irc.votebot.entity.Poll;
import org.freecode.irc.votebot.entity.Vote;

import java.io.*;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Shivam
 * Date: 17/06/13
 * Time: 00:05
 */
public class FreeVoteBot implements PrivateMessageListener {
    public static final double VERSION = 1.03D;
    public static final String CHANNEL_SOURCE = "#freecode";

    private PollDAO pollDAO;
    private VoteDAO voteDAO;
    private String[] channels;
    private String nick, realName, serverHost, user;
    private int port;

    private IrcConnection connection;

    private ExpiryQueue<String> expiryQueue = new ExpiryQueue<>(1500L);
    private LinkedList<FVBModule> moduleList = new LinkedList<>();

    public void init() {
        connectToIRCServer();
        addNickInUseListener();
        registerUser();
        addCTCPRequestListener();
        identifyToNickServ();
        joinChannels();
    }

    private void registerUser() {
        try {
            connection.register(nick, user, realName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.addListener(this);
    }

    private void addNickInUseListener() {
        NumericListener nickInUse = new NumericListener() {
            public int getNumeric() {
                return IrcConnection.ERR_NICKNAMEINUSE;
            }

            public void execute(String rawLine) {
                FreeVoteBot.this.nick = FreeVoteBot.this.nick + "_";
                try {
                    connection.sendRaw("NICK " + FreeVoteBot.this.nick);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        connection.addListener(nickInUse);
    }

    private void addCTCPRequestListener() {
        connection.addListener(new CtcpRequestListener() {
            public void onCtcpRequest(CtcpRequest request) {
                if (request.getCommand().equals("VERSION")) {
                    request.getIrcConnection().send(new CtcpResponse(request.getIrcConnection(),
                            request.getNick(), "VERSION", "FreeVoteBot " + VERSION + " by " + CHANNEL_SOURCE + " on irc.rizon.net"));
                } else if (request.getCommand().equals("PING")) {
                    request.getIrcConnection().send(new CtcpResponse(request.getIrcConnection(),
                            request.getNick(), "PING", request.getArguments()));
                }
            }
        });
    }

    private void connectToIRCServer() {
        try {
            connection = new IrcConnection(serverHost, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void identifyToNickServ() {
        File pass = new File("password.txt");
        if (pass.exists()) {
            try {
                BufferedReader read = new BufferedReader(new FileReader(pass));
                String s = read.readLine();
                if (s != null) {
                    connection.send(new Privmsg("NickServ", "identify " + s, connection));
                }
                read.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void joinChannels() {
        for (String channel : channels) {
            connection.joinChannel(channel);
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

                    Poll poll = pollDAO.getOpenPoll(pollId);
                    if (poll != null) {
                        long time = poll.getExpiry();
                        if (System.currentTimeMillis() < time) {
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

    public void onPrivmsg(final Privmsg privmsg) {
        try {
            final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.UK);
            SDF.setTimeZone(TimeZone.getTimeZone("Europe/London"));
            final String message = privmsg.getMessage().toLowerCase();

            for (FVBModule module : moduleList) {
                if (module.isEnabled() && module.canRun(privmsg)) {
                    module.process(privmsg);
                }
            }

            String sender = privmsg.getNick().toLowerCase();
            if (expiryQueue.contains(sender)) {
                return;
            } else {
                expiryQueue.insert(sender);
            }

            if (message.startsWith("!v ") || message.startsWith("!vote ")) {
                final String msg = privmsg.getMessage().substring(privmsg.getMessage().indexOf(' ')).trim();
                System.out.println(msg);
                final String[] split = msg.split(" ", 2);
                if (split.length == 2) {
                    String ids = split[0];
                    String vote = split[1].toLowerCase();
                    if (!vote.equalsIgnoreCase("yes") && !vote.equalsIgnoreCase("no") && !vote.equalsIgnoreCase("abstain")) {
                        return;
                    }
                    final int nId;
                    if (vote.equalsIgnoreCase("yes")) {
                        nId = 0;
                    } else if (vote.equalsIgnoreCase("no")) {
                        nId = 1;
                    } else {
                        nId = 2;
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

                    int pollId = Integer.parseInt(id);
                    Poll poll = pollDAO.getPoll(pollId);

                    if (poll != null) {
                        String expiry = SDF.format(new Date(poll.getExpiry()));
                        String closed = poll.isClosed() ? "Closed" : "Open";
                        if (System.currentTimeMillis() >= poll.getExpiry()) {
                            closed = "Expired";
                        }

                        Vote[] votes = voteDAO.getVotesOnPoll(pollId);
                        int yes = 0, no = 0, abstain = 0;
                        for (Vote vote : votes) {
                            int answerIndex = vote.getAnswerIndex();
                            if (answerIndex == 0) {
                                yes++;
                            } else if (answerIndex == 1) {
                                no++;
                            } else if (answerIndex == 2) {
                                abstain++;
                            }
                        }

                        boolean open = closed.equals("Open");
                        privmsg.send(poll.getQuestion() +
                                " Options: " + poll.getOptions() + " Created by: " + poll.getCreator() +
                                " Yes: " + yes + " No: " + no + " Abstain: " + abstain +
                                " Status: \u00030" + (open ? "3" : "4") + closed + "\u0003" +
                                (open ? " Ends: " : " Ended: ") + expiry);
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
            } else if (message.equals("!polls")) {
                showOpenPolls(privmsg);
            } else if (message.startsWith("!closepoll ")) {
                String[] parts = message.split(" ", 2);
                if (parts.length != 2 || !parts[1].matches("\\d+")) {
                    return;
                }

                final int id = Integer.parseInt(parts[1]);
                privmsg.getIrcConnection().addListener(new NoticeFilter() {
                    public boolean accept(Notice notice) {
                        Pattern pattern = Pattern.compile("\u0002(.+?)\u0002");
                        Matcher matcher = pattern.matcher(notice.getMessage());
                        if (matcher.find() && matcher.find()) {
                            String access = matcher.group(1);
                            if (access.equals("AOP") || access.equals("Founder") || access.equals("SOP")) {
                                return notice.getNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains("\u0002" + privmsg.getNick() + "\u0002");
                            }
                        }
                        if (notice.getMessage().equals("Permission denied."))
                            notice.getIrcConnection().removeListener(this);
                        return false;
                    }

                    public void run(Notice notice) {
                        try {
                            if (pollDAO.setStatusOfPoll(id, true)) {
                                privmsg.send("Poll closed.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        privmsg.getIrcConnection().removeListener(this);
                    }
                });
                askChanServForUserCreds(privmsg);
            } else if (message.startsWith("!openpoll ")) {
                String[] parts = message.split(" ", 2);
                if (parts.length != 2 || !parts[1].matches("\\d+")) {
                    return;
                }

                final int id = Integer.parseInt(parts[1]);
                privmsg.getIrcConnection().addListener(new NoticeFilter() {
                    public boolean accept(Notice notice) {
                        Pattern pattern = Pattern.compile("\u0002(.+?)\u0002");
                        Matcher matcher = pattern.matcher(notice.getMessage());
                        if (matcher.find() && matcher.find()) {
                            String access = matcher.group(1);
                            if (access.equals("AOP") || access.equals("Founder") || access.equals("SOP")) {
                                return notice.getNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains("\u0002" + privmsg.getNick() + "\u0002");
                            }
                        }
                        if (notice.getMessage().equals("Permission denied."))
                            notice.getIrcConnection().removeListener(this);
                        return false;
                    }

                    public void run(Notice notice) {
                        try {
                            if (pollDAO.setStatusOfPoll(id, false)) {
                                privmsg.send("Poll opened.");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        privmsg.getIrcConnection().removeListener(this);
                    }
                });
                askChanServForUserCreds(privmsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void askChanServForUserCreds(Privmsg privmsg) {
        privmsg.getIrcConnection().send(new Privmsg("ChanServ", "WHY " + CHANNEL_SOURCE + " " + privmsg.getNick(), privmsg.getIrcConnection()));
    }

    private void showOpenPolls(final Privmsg privmsg) throws SQLException {
        SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.UK);
        SDF.setTimeZone(TimeZone.getTimeZone("Europe/London"));

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
                    " Ends: " + SDF.format(new Date(poll.getExpiry())) + " Created by: " + poll.getCreator() +
                    " Yes: " + yes + " No: " + no + " Abstain: " + abstain;
            privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), msg, privmsg.getIrcConnection()));
        }
        privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "End list of polls.", privmsg.getIrcConnection()));
    }

    public void setPollDAO(PollDAO pollDAO) {
        this.pollDAO = pollDAO;
    }

    public void setVoteDAO(VoteDAO voteDAO) {
        this.voteDAO = voteDAO;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    public void setChannels(String channels) {
        this.channels = channels.split(",");
    }

    public void setModules(final FVBModule[] modules) {
        moduleList.clear();
        moduleList.addAll(Arrays.asList(modules));
    }
}
