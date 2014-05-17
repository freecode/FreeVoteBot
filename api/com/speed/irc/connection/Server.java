package com.speed.irc.connection;

import com.speed.irc.connection.ssl.IRCTrustManager;
import com.speed.irc.event.EventManager;
import com.speed.irc.event.api.ApiEvent;
import com.speed.irc.types.*;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class representing a socket connection to an IRC server with the
 * functionality of sending raw commands and messages.
 * <p/>
 * This file is part of Speed's IRC API.
 * <p/>
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * <p/>
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Shivam Mistry
 */
public class Server implements Runnable {
    private volatile BufferedWriter write;
    private volatile BufferedReader read;
    protected volatile Socket socket;
    protected EventManager eventManager = new EventManager();
    protected Map<String, Channel> channels = new HashMap<String, Channel>();
    private static SSLContext context;
    private List<ServerUser> users;
    private char[] modeSymbols;
    private char[] channelPrefix;
    private char[] modeLetters;
    private String serverName;
    private String nick, realName, user;
    private ServerMessageParser parser;
    protected HashSet<CTCPReply> ctcpReplies = new HashSet<CTCPReply>();
    public Map<ServerUser, Collection<RawMessage>> whoisWaiting = new HashMap<ServerUser, Collection<RawMessage>>();
    protected boolean autoConnect;
    private int port;
    private ScheduledThreadPoolExecutor chanExec;
    private ScheduledExecutorService serverExecutor, eventExecutor;
    private ModeList userModes;

    /**
     * Initialises a server object. Only blocking IO is supported.
     *
     * @param sock The socket used for communication to the IRC server.
     * @throws IOException
     */
    public Server(final Socket sock) throws IOException {
        socket = sock;
        port = sock.getPort();
        setServerName(socket.getRemoteSocketAddress().toString());
        write = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        read = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        chanExec = new ScheduledThreadPoolExecutor(10);
        serverExecutor = Executors.newSingleThreadScheduledExecutor();
        eventExecutor = Executors.newSingleThreadScheduledExecutor();
        serverExecutor.scheduleWithFixedDelay(this, 1000, 200, TimeUnit.MILLISECONDS);
        eventExecutor.scheduleWithFixedDelay(eventManager, 1000, 100, TimeUnit.MILLISECONDS);
        parser = new ServerMessageParser(this);
        users = new CopyOnWriteArrayList<ServerUser>();
        ctcpReplies.add(ServerMessageParser.CTCP_REPLY_VERSION);
        ctcpReplies.add(ServerMessageParser.CTCP_REPLY_TIME);
        ctcpReplies.add(ServerMessageParser.CTCP_REPLY_PING);
    }

    public Server(final String host, final int port) throws IOException {
        this(new Socket(host, port));

    }

    public Server(final String host, final int port, final boolean ssl) throws IOException {
        this(ssl ? context.getSocketFactory().createSocket(host, port) : new Socket(host, port));
    }

    static {
        try {
            context = SSLContext.getInstance("SSL");
            context.init(new KeyManager[0], new TrustManager[]{new IRCTrustManager()}, new SecureRandom());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public void parseUserModes(final String modes) {
        userModes.parse(modes);
    }

    public ModeList getUserModes() {
        return userModes;
    }

    public boolean isUsingSSL() {
        return socket instanceof SSLSocket;
    }

    public SSLSocket getSSLSocket() {
        return isUsingSSL() ? (SSLSocket) socket : null;
    }

    public String getRealName() {
        return realName;
    }

    public String getUser() {
        return user;
    }

    /**
     * Gets the channel thread executor, used to send WHO requests for channels.
     *
     * @return the channel thread executor
     */
    public ScheduledThreadPoolExecutor getChanExec() {
        return chanExec;
    }

    /**
     * Sends a QUIT command (with no message) to the server and shuts down this
     * server connection.
     */
    public void quit() {
        quit(null);
    }

    public void setNick(final String newNick) {
        sendRaw("NICK " + newNick);
    }

    protected void putNick(final String nick) {
        this.nick = nick;
    }

    public void register(final String nick) {
        register(nick, null, null, null);
    }

    public void register(final String nick, final String user) {
        register(nick, user, null, null);
    }

    public void register(final String nick, final String user, final String realName) {
        register(nick, user, realName, null);
    }

    public void register(final String nick, String user, String realName, final String pass) {
        if (nick == null || nick.isEmpty()) {
            quit();
            throw new IllegalArgumentException("Nickname is null or empty");
        }
        if (pass != null && !pass.isEmpty()) {
            sendRaw("PASS " + pass);
        }
        if (user == null || user.isEmpty()) {
            user = nick;
        }
        if (realName == null || realName.isEmpty()) {
            realName = user;
        }
        setNick(nick);
        sendRaw("USER " + user + " 0 * :" + realName);
        this.nick = nick;
        this.realName = realName;
        this.user = user;
    }

    /**
     * Sends a QUIT command to the server and shuts down this server connection.
     *
     * @param message the quit message to send to the server, <tt>null</tt> or
     *                <tt>""</tt> for no message
     */
    public void quit(final String message) {
        eventManager.dispatchEvent(new ApiEvent(ApiEvent.SERVER_QUIT, this, this));
        parser.reader.running = false;
        try {
            if (!socket.isClosed()) {
                getWriter().write(
                        "QUIT" + (message == null || message.trim().isEmpty() ? "\n" : (" :" + message + "\n")));
                getWriter().flush();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        for (Channel c : channels.values()) {
            if (c.getFuture() != null && !c.getFuture().isDone())
                c.getFuture().cancel(true);
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        eventExecutor.shutdownNow();
        parser.execServ.shutdownNow();
        chanExec.shutdownNow();
        serverExecutor.shutdownNow();
    }

    /**
     * Sets the logger to log debug output to and turns debugging on.
     *
     * @param logger the logger to log output to.
     */
    public final void setReadDebug(final Logger logger) {
        parser.reader.logger = logger;
        setReadDebug(true);
    }

    /**
     * Controls whether the API should log debug output.
     *
     * @param on <tt>true</tt> to enable debug output, <tt>false</tt> otherwise
     */
    public final void setReadDebug(boolean on) {
        parser.reader.logging = on;
    }

    protected final void connect() {
        try {
            socket = new Socket(serverName, port);
            write = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Logger logger = null;
            boolean log = false;
            if (parser.reader.logging) {
                logger = parser.reader.logger;
                log = parser.reader.logging;
            }
            parser = new ServerMessageParser(this);
            if (logger != null && log) {
                setReadDebug(logger);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerMessageParser getParser() {
        return parser;
    }

    public CTCPReply getCtcp(String request) {
        synchronized (ctcpReplies) {
            for (CTCPReply reply : ctcpReplies) {
                if (reply.getRequest().equals(request)) {
                    return reply;
                }
            }
        }
        return null;
    }

    /**
     * Sets whether the api should auto reconnect if the connection is broken.
     * Default is <i>off</i>.
     *
     * @param on
     */
    public void setAutoReconnect(final boolean on) {
        this.autoConnect = on;
    }

    /**
     * Gets the current nick as captured by the message sending thread.
     *
     * @return the current nick for this server connection.
     */
    public String getNick() {
        return nick;
    }

    /**
     * Sets a reply to a CTCP request.
     *
     * @param request the request to send the reply for
     * @param reply   the reply to send for the request
     */
    public void setCtcpReply(final String request, final String reply) {
        synchronized (ctcpReplies) {
            ctcpReplies.add(new CTCPReply() {

                public String getReply() {
                    return reply;
                }

                public String getRequest() {
                    return request;
                }

            });
        }
    }

    public void removeCtcpReply(final CTCPReply reply) {
        synchronized (ctcpReplies) {
            ctcpReplies.remove(reply);
        }
    }

    /**
     * Adds an automated CTCP reply to the reply list.
     *
     * @param reply the CTCPReply to be added to the list
     */
    public void addCtcpReply(final CTCPReply reply) {
        synchronized (ctcpReplies) {
            ctcpReplies.add(reply);
        }
    }

    /**
     * Gets the reply which corresponds to the request.
     *
     * @param request the request to retrieve the reply for
     * @return the reply for the supplied request
     */
    public String getCtcpReply(final String request) {
        synchronized (ctcpReplies) {
            for (CTCPReply reply : ctcpReplies) {
                Matcher matcher = Pattern.compile(reply.getRequest(), Pattern.CASE_INSENSITIVE).matcher(request);
                if (matcher.matches()) {
                    if (matcher.groupCount() == 0)
                        return reply.getReply();
                    else {
                        String resp = reply.getReply();
                        StringBuffer response = new StringBuffer();
                        boolean flag = false;
                        for (int i = 0; i < resp.length(); i++) {
                            char c = resp.charAt(i);
                            if (c == '$' && (i == 0 || resp.charAt(i - 1) != '\\')) {
                                flag = true;
                                continue;
                            } else if (resp.charAt(i - 1) == '\\') {
                                response.deleteCharAt(i - 1);
                            } else if (Character.isDigit(c) && flag) {
                                int group = Character.getNumericValue(c);
                                flag = false;
                                try {
                                    String str = matcher.group(group);
                                    response.append(str);
                                } catch (IndexOutOfBoundsException e) {
                                    e.printStackTrace();
                                }
                                continue;

                            }
                            response.append(c);

                        }
                        return response.toString();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sends a raw command to the server.
     *
     * @param raw The raw command to be added to the sending queue.
     */
    public void sendRaw(String raw) {
        if (raw.startsWith("NICK")) {
            nick = raw.replace("NICK", "").replace(":", "").trim();
        }
        if ((raw.contains("\n") || raw.contains("\r")) && !raw.endsWith("\r\n"))
            raw = raw.replace("\n", "").replace("\r", "");
        if (!raw.endsWith("\r\n"))
            raw += "\r\n";
        try {
            // System.out.println(raw);
            write.write(raw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the channel map.
     *
     * @return the channel map.
     */
    protected Map<String, Channel> getChannelMap() {
        return channels;
    }

    public Collection<Channel> getChannels() {
        return channels.values();
    }

    public void addChannel(final Channel channel) {
        channels.put(channel.getName().toLowerCase(), channel);
    }

    /**
     * Gets the buffered writer.
     *
     * @return the buffered writer.
     */
    public BufferedWriter getWriter() {
        return write;
    }

    /**
     * Sets the buffered writer.
     *
     * @param write the new buffered writer.
     */
    public void setWrite(final BufferedWriter write) {
        this.write = write;
    }

    /**
     * Gets the buffered reader.
     *
     * @return the buffered reader.
     */
    public BufferedReader getReader() {
        return read;
    }

    /**
     * Sets the buffered reader.
     *
     * @param read the new buffered reader.
     */
    public void setRead(final BufferedReader read) {
        this.read = read;
    }

    /**
     * Checks whether the api is connected to the server.
     *
     * @return <code>true</code> if we are connected, <code>false</code> if
     * unconnected.
     */
    public boolean isConnected() {
        return !socket.isClosed();
    }

    /**
     * Gets the channel access mode symbols (e.g. @ for op)
     *
     * @return the channel access mode symbols.
     */
    public char[] getModeSymbols() {
        return modeSymbols;
    }

    protected void setModeSymbols(final char[] modeSymbols) {
        this.modeSymbols = modeSymbols;
    }

    /**
     * Gets the channel access mode letters (e.g. v for voice)
     *
     * @return the channel access mode letters
     */
    public char[] getModeLetters() {
        return modeLetters;
    }

    protected void setModeLetters(final char[] modeLetters) {
        this.modeLetters = modeLetters;
    }

    /**
     * Sends a notice to the specified nick.
     *
     * @param notice the notice to send, sender can be null.
     */
    public void sendNotice(final Notice notice) {
        sendRaw("NOTICE " + notice.getTarget() + " :" + notice.getMessage() + "\n");
    }

    /**
     * Sends a private message to the server.
     *
     * @param msg the message to send, sender can be null.
     */
    public void sendMessage(final Privmsg msg) {
        sendRaw(String.format("PRIVMSG %s :%s", msg.getConversable().getName(), msg.getMessage()));
    }

    /**
     * Gets a server user object with a supplied nickname.
     *
     * @param nick the nickname to search for
     * @return the ServerUser object, creates a new object if the user wasn't
     * found.
     */
    public ServerUser getUser(final String nick) {
        for (ServerUser u : users) {
            if (u.getNick().equalsIgnoreCase(nick))
                return u;
        }
        return new ServerUser(nick, null, null, this);
    }

    /**
     * Sends an action to a channel/nick.
     *
     * @param channel The specified channel/nick you would like to send the action
     *                to.
     * @param action  The action you would like to send.
     */
    public void sendAction(final String channel, final String action) {
        sendRaw("PRIVMSG " + channel + ": \u0001ACTION " + action + "\n");
    }

    /**
     * Gets the event manager associated with this server object.
     *
     * @return the event manager for this server.
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    public void run() {
        try {
            if (write != null) {
                write.flush();
            }
        } catch (SocketException e) {
            if (autoConnect) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                connect();
                eventManager.dispatchEvent(new ApiEvent(ApiEvent.SERVER_DISCONNECTED, this, this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Sets the server's host address.
     *
     * @param serverName the server's host address.
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Gets the server's host address.
     *
     * @return the server's host address.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Joins a channel on this server if we are not already joined to it.
     *
     * @param channelName The name of the channel.
     * @return The channel object.
     */
    public Channel joinChannel(final String channelName) {
        if (channels.containsKey(channelName.trim())) {
            final Channel channel = channels.get(channelName.toLowerCase());
            if (!channel.isRunning()) {
                channel.join();
            }
            return channel;
        }
        final Channel channel = new Channel(channelName, this);
        channel.join();
        return channel;
    }

    /**
     * Creates/finds a channel object for the specified channel.
     *
     * @param channelName the name of the channel.
     * @return a channel object.
     */
    public Channel getChannel(final String channelName) {
        return channels.containsKey(channelName.toLowerCase().trim()) ? channels.get(channelName) : new Channel(
                channelName, this);
    }

    /**
     * Adds a user to the Server's list.
     *
     * @param user the {@link com.speed.irc.types.ServerUser} object to add to
     *             this Server.
     */
    public void addUser(final ServerUser user) {
        users.add(user);
    }

    protected void removeUser(final ServerUser user) {
        users.remove(user);
    }

    public boolean hasChannel(final String name) {
        return channels.containsKey(name.toLowerCase());
    }

    public boolean hasChannel(final Channel channel) {
        return channels.containsValue(channel);
    }

    public void addWhoisWaiting(final ServerUser c) {
        whoisWaiting.put(c, new LinkedList<RawMessage>());
    }

    public char[] getChannelPrefix() {
        return parser.getServerSupport().getChanTypes();
    }
}
