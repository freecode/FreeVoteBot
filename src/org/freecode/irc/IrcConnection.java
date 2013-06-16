package org.freecode.irc;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: Shivam
 * Date: 16/06/13
 * Time: 22:30
 */
public class IrcConnection implements Runnable {

    private BufferedReader reader;
    private BufferedWriter writer;
    private Socket socket;
    private String host;
    private int port;
    private volatile List<RawIrcListener> listeners;
    private ScheduledExecutorService executor;
    private Future future;
    private static final int ERR_NICKNAMEINUSE = 433;

    public IrcConnection(final String host, final int port) throws IOException {
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        listeners = new LinkedList<RawIrcListener>();
        executor = Executors.newSingleThreadScheduledExecutor();
        future = executor.scheduleAtFixedRate(this, 100L, 100L, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        String nick = "FreeVoteBot";
        String user = "FreeVoteBot";
        String realName = "FreeVoteBot";
        String serverHost = "irc.rizon.net";
        int port = 6667;
        String[] chans = new String[]{"#freecode"};
        if (args.length % 2 == 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                String nextArg = args[++i];
                if (arg.equalsIgnoreCase("-nick") || arg.equalsIgnoreCase("-n")) {
                    nick = nextArg;
                } else if (arg.equalsIgnoreCase("-user") || arg.equalsIgnoreCase("-u")) {
                    user = nextArg;
                } else if (arg.equalsIgnoreCase("-realname") || arg.equalsIgnoreCase("-r")) {
                    realName = nextArg;
                } else if (arg.equalsIgnoreCase("-host") || arg.equalsIgnoreCase("-h")) {
                    serverHost = nextArg;
                } else if (arg.equalsIgnoreCase("-port") || arg.equalsIgnoreCase("-p")) {
                    try {
                        port = Integer.parseInt(nextArg);
                    } catch (NumberFormatException e) {
                        System.out.println("Failed to parse port: " + nextArg);
                        System.out.println("Using default port: " + port);
                    }
                } else if (arg.equalsIgnoreCase("-channels") || arg.equalsIgnoreCase("-c")) {
                    chans = nextArg.split(",");
                }
            }
        } else {
            System.out.println("Incorrect argument count, using defaults.");
        }
        try {
            final IrcConnection connection = new IrcConnection(serverHost, port);
            //nick in use listener, should be removed so client can handle this after
            final String finNick = nick;
            NumericListener nickInUse = new NumericListener() {
                public int getNumeric() {
                    return ERR_NICKNAMEINUSE;
                }

                public void execute(String rawLine) {
                    String nick = finNick + "_";
                    try {
                        connection.sendRaw("NICK " + nick);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            connection.addListener(nickInUse);
            connection.register(nick, user, realName);
            connection.removeListener(nickInUse);
            for (String channel : chans) {
                connection.joinChannel(channel);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void joinChannel(String channel) {
        try {
            sendRaw("JOIN :" + channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addListener(final RawIrcListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final RawIrcListener listener) {
        listeners.remove(listener);
    }

    public void register(final String nick, final String user, final String realName) throws IOException {
        sendRaw("NICK " + nick);
        sendRaw("USER " + user + " 0 * :" + realName);
    }

    public void sendRaw(String s) throws IOException {
        if (s.endsWith("\n")) {
            s = s.replaceAll("[\n\r]", "");
        }
        if (!socket.isOutputShutdown()) {
            writer.write(s);
            writer.newLine();
            writer.flush();
        }
    }


    public void run() {
        if (socket.isInputShutdown()) {
            future.cancel(true);
        } else {
            try {
                String raw = reader.readLine();
                System.out.println(raw);
                if (!Character.isLetterOrDigit(raw.charAt(0))) {
                    raw = raw.substring(1);
                }
                if (raw.startsWith("PING ")) {
                    sendRaw(raw.replaceFirst("PING", "PONG"));
                } else {
                    for (RawIrcListener listener : listeners) {
                        if (listener.qualifies(raw)) {
                            listener.execute(raw);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
