package org.freecode.irc.votebot;

import org.freecode.irc.CtcpRequest;
import org.freecode.irc.CtcpResponse;
import org.freecode.irc.IrcConnection;
import org.freecode.irc.Privmsg;
import org.freecode.irc.event.CtcpRequestListener;
import org.freecode.irc.event.NumericListener;
import org.freecode.irc.event.PrivateMessageListener;
import org.freecode.irc.votebot.api.AdminModule;
import org.freecode.irc.votebot.api.FVBModule;
import org.freecode.irc.votebot.modules.admin.LoadModules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * User: Shivam
 * Date: 17/06/13
 * Time: 00:05
 */
public class FreeVoteBot implements PrivateMessageListener {
    public static final String CHANNEL_SOURCE = "#freecode";

    private String[] channels;
    private String nick, realName, serverHost, user;
    private int port;
    private ScriptModuleLoader sml;
    private IrcConnection connection;
    private String version;

    private ExpiryQueue<String> expiryQueue = new ExpiryQueue<>(1500L);
    private LinkedList<FVBModule> moduleList = new LinkedList<>();

    public void init() {
        connectToIRCServer();
        NoticeFilter.setFilterQueue(connection, 5000L);
        addNickInUseListener();
        registerUser();
        addCTCPRequestListener();
        identifyToNickServ();
        joinChannels();
        sml = new ScriptModuleLoader(this);
        AdminModule mod = new LoadModules();
        mod.setFvb(this);
        moduleList.add(mod);
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
                            request.getNick(), "VERSION", "FreeVoteBot " + version + " by " + CHANNEL_SOURCE + " on irc.rizon.net"));
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

    public void onPrivmsg(final Privmsg privmsg) {
        if (privmsg.getNick().equalsIgnoreCase(nick)) {
            return;
        }

        String sender = privmsg.getNick().toLowerCase();
        if (expiryQueue.contains(sender) || !expiryQueue.insert(sender)) {
            return;
        }

        for (FVBModule module : moduleList) {
            try {
                if (module.isEnabled() && module.canRun(privmsg)) {
                    module.process(privmsg);
                    return;
                }
            } catch (Exception e) {
                privmsg.send(e.getMessage());
            }
        }

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

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean addModule(final FVBModule module) {
        return moduleList.add(module);
    }

    public void addModules(final Collection<? extends FVBModule> module) {
        moduleList.addAll(module);
    }

    public boolean removeModule(final FVBModule module) {
        return moduleList.remove(module);
    }

    public void removeModules(final Collection<? extends FVBModule> module) {
        moduleList.removeAll(module);
    }

    public ScriptModuleLoader getScriptModuleLoader() {
        return sml;
    }
}
