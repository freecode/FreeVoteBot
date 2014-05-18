package org.freecode.irc.votebot;

import com.speed.irc.connection.Server;
import com.speed.irc.event.channel.ChannelUserEvent;
import com.speed.irc.event.channel.ChannelUserListener;
import com.speed.irc.event.channel.ModeChangedEvent;
import com.speed.irc.event.message.PrivateMessageEvent;
import com.speed.irc.event.message.PrivateMessageListener;
import com.speed.irc.event.message.RawMessageEvent;
import com.speed.irc.event.message.RawMessageListener;
import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;
import org.freecode.irc.votebot.api.FVBModule;
import org.freecode.irc.votebot.dao.PollDAO;
import org.freecode.irc.votebot.dao.VoteDAO;
import org.freecode.irc.votebot.entity.Poll;
import org.freecode.irc.votebot.entity.Vote;
import org.freecode.irc.votebot.modules.admin.LoadModules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * User: Shivam
 * Date: 17/06/13
 * Time: 00:05
 */

@Component
public class FreeVoteBot implements PrivateMessageListener, ChannelUserListener {
    public static final String CHANNEL_SOURCE = "#freecode";

    @Value("${irc.channel}")
    private String channel;

    @Value("${user.nick}")
    private String nick;
    @Value("${user.userName}")
    private String user;
    @Value("${user.realName}")
    private String realName;

    @Value("${irc.host}")
    private String serverHost;
    @Value("${irc.port}")
    private int port;

    private ScriptModuleLoader sml;
    private Server connection;

    @Value("${git.commit.id.describe}")
    private String version;

    private ExpiryQueue<String> expiryQueue = new ExpiryQueue<>(1500L);

    @Autowired
    private List<FVBModule> modules;// = new LinkedList<>();

    @Autowired
    private PollDAO pollDAO;

    @Autowired
    private VoteDAO voteDAO;

    @Autowired
    private PropertyStore propertyStore;

    public ScheduledExecutorService pollExecutor;
    public HashMap<Integer, Future> pollFutures;
    public static final int ERR_NICKNAMEINUSE = 433;

    @PostConstruct
    public void init() {
        connectToIRCServer();
        NoticeFilter.setFilterQueue(connection, 5000L);
        addNickInUseListener();
        registerUser();
        addCTCPRequestListener();
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        identifyToNickServ();
        joinChannels();

        propertyStore.load();

        sml = new ScriptModuleLoader(this);
        AdminModule mod = new LoadModules();
        mod.setFvb(this);
        modules.add(mod);
        pollExecutor = Executors.newScheduledThreadPool(5);
        pollFutures = new HashMap<>();
        try {
            for (Poll poll : pollDAO.getOpenPolls()) {
                long expiry = poll.getExpiry();
                int id = poll.getId();
                PollExpiryAnnouncer announcer = new PollExpiryAnnouncer(expiry, id, this);
                ScheduledFuture future = pollExecutor.scheduleAtFixedRate(announcer, 60000L, 500L, TimeUnit.MILLISECONDS);
                announcer.setFuture(future);
                pollFutures.put(id, future);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        for(FVBModule module : modules) {
            module.onConnect();
        }

    }

    private void registerUser() {
        connection.register(nick, user, realName);
        connection.getEventManager().addListener(this);
    }

    private void addNickInUseListener() {
        RawMessageListener nickInUse = new RawMessageListener() {

            public void rawMessageReceived(RawMessageEvent e) {
                if (e.getMessage().getCommand().equalsIgnoreCase(String.valueOf(ERR_NICKNAMEINUSE))) {
                    FreeVoteBot.this.nick = FreeVoteBot.this.nick + "_";
                    connection.sendRaw("NICK " + FreeVoteBot.this.nick);

                }
            }
        };
        connection.getEventManager().addListener(nickInUse);
    }

    private void addCTCPRequestListener() {
        connection.setCtcpReply("VERSION", "FreeVoteBot " + version);
    }

    private void connectToIRCServer() {
        try {
            connection = new Server(serverHost, port, true);
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
                    connection.sendMessage(new Privmsg("identify " + s, null, connection.getUser("NickServ")));
                }
                read.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void joinChannels() {
        //for (String channel : channels) {
        connection.joinChannel(channel);
        //}
    }

    public void onPrivmsg(final Privmsg privmsg) {


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

    public void setChannels(String[] channels) {
        //this.channels = channels.split(",");
        //this.channels=channels;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setPropertyStore(PropertyStore propertyStore) {
        this.propertyStore = propertyStore;
    }

    public void setModules(final FVBModule[] modules) {
        this.modules.clear();
        this.modules.addAll(Arrays.asList(modules));
        for (FVBModule module : this.modules) {
            if (module instanceof AdminModule) {
                ((AdminModule) module).setFvb(this);
            }
        }

    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean addModule(final FVBModule module) {
        return modules.add(module);
    }

    public void addModules(final Collection<? extends FVBModule> module) {
        modules.addAll(module);
        for (FVBModule mod : modules) {
            if (mod instanceof AdminModule) {
                ((AdminModule) mod).setFvb(this);
            }
        }
    }

    public boolean removeModule(final FVBModule module) {
        return modules.remove(module);
    }

    public void removeModules(final Collection<? extends FVBModule> module) {
        modules.removeAll(module);
    }

    public ScriptModuleLoader getScriptModuleLoader() {
        return sml;
    }

    public void sendMsg(String s) {
/*        if(channels==null) {
            //throw new RuntimeException("This shit right here");
            System.out.println("This shit right here");
            return;
        }*/
//        for (String channel : channels) {

        if(channel==null) {
            System.out.println("This shit right here");
            return;
        }

        connection.sendMessage(new Privmsg(s, null, connection.getChannel(channel)));
        //      }
    }

    public void setPollDAO(PollDAO pollDAO) {
        this.pollDAO = pollDAO;
    }

    public PollDAO getPollDAO() {
        return pollDAO;
    }

    public void setVoteDAO(VoteDAO voteDAO) {
        this.voteDAO = voteDAO;
    }

    public VoteDAO getVoteDAO() {
        return voteDAO;
    }

    private DateFormat getDateFormatter() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        return dateFormat;
    }

    @Override
    public void messageReceived(PrivateMessageEvent e) {
        Privmsg privmsg = e.getMessage();
        if (privmsg.getSender().equalsIgnoreCase(nick)) {
            return;
        }

        String sender = privmsg.getSender().toLowerCase();
        if (expiryQueue.contains(sender) || !expiryQueue.insert(sender)) {
            return;
        }

        for (FVBModule module : modules) {
            try {
                if (module.isEnabled() && module.canRun(privmsg)) {
                    module.process(privmsg);
                    return;
                }
            } catch (Exception e1) {
                privmsg.getConversable().sendMessage(e1.getMessage());
            }
        }
    }

    public void channelUserJoined(ChannelUserEvent e) {
        String nick = e.getUser().getNick();
        String channel = e.getChannel().getName();
        try {
            Poll[] openPolls = pollDAO.getOpenPolls();
            Poll[] pollsNotVotedIn = voteDAO.getPollsNotVotedIn(openPolls, nick);
            PollVotes[] pollVotes = new PollVotes[pollsNotVotedIn.length];
            for (int i = 0; i < pollsNotVotedIn.length; i++) {
                Poll poll = pollsNotVotedIn[i];
                String question = poll.getQuestion();
                int id = poll.getId();
                long expiry = poll.getExpiry();
                Date date = new Date(expiry);
                Vote[] votes = voteDAO.getVotesOnPoll(id);
                String msg = String.format("Open poll #%d: \"%s\", ends: %s, votes: %d", id, question, getDateFormatter().format(date), votes.length);
                pollVotes[i] = new PollVotes(votes.length, msg);
            }
            if (pollVotes.length == 0) {
                e.getUser().sendNotice("No new polls to vote in!");
            } else {
                Arrays.sort(pollVotes);
                e.getUser().sendNotice("Trending polls list:");
                if (pollVotes.length >= 3) {
                    e.getUser().sendNotice(pollVotes[0].question);
                    e.getUser().sendNotice(pollVotes[1].question);
                    e.getUser().sendNotice(pollVotes[2].question);
                } else {
                    for (PollVotes p : pollVotes) {
                        e.getUser().sendNotice(p.question);
                    }
                }
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    public void channelUserParted(ChannelUserEvent e) {

    }

    public void channelUserModeChanged(ModeChangedEvent e) {

    }

    public void channelUserKicked(ChannelUserEvent e) {

    }

    public void channelUserNickChanged(ChannelUserEvent e) {

    }

    public void channelUserQuit(ChannelUserEvent e) {

    }

    static class PollVotes implements Comparable<PollVotes> {
        int votes;
        String question;

        PollVotes(int votes, String question) {
            this.votes = votes;
            this.question = question;
        }

        public int compareTo(PollVotes o) {
            return o.votes - votes;
        }
    }

    public void onJoin(String channel, String nick, String mask) {

    }
}
