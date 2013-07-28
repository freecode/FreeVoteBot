package org.freecode.irc.votebot;

import org.freecode.irc.IrcConnection;
import org.freecode.irc.Privmsg;
import org.freecode.irc.event.NumericListener;
import org.freecode.irc.event.PrivateMessageListener;

import java.io.IOException;

/**
 * User: Shivam
 * Date: 17/06/13
 * Time: 00:05
 */
public class FreeVoteBot {

    public static void main(String[] args) {
        String nick = null, user = null, realName = null, serverHost = null;
        int port = 6667;
        String[] chans = new String[]{"#freecode"};
        if (args.length % 2 == 0 && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                String nextArg = args[++i];
                if (arg.equalsIgnoreCase("--nick") || arg.equalsIgnoreCase("-n")) {
                    nick = nextArg;
                } else if (arg.equalsIgnoreCase("--user") || arg.equalsIgnoreCase("-u")) {
                    user = nextArg;
                } else if (arg.equalsIgnoreCase("--realname") || arg.equalsIgnoreCase("-r")) {
                    realName = nextArg;
                } else if (arg.equalsIgnoreCase("--host") || arg.equalsIgnoreCase("-h")) {
                    serverHost = nextArg;
                } else if (arg.equalsIgnoreCase("--port") || arg.equalsIgnoreCase("-p")) {
                    try {
                        port = Integer.parseInt(nextArg);
                    } catch (NumberFormatException e) {
                        System.out.println("Failed to parse port: " + nextArg);
                        System.out.println("Using default port: " + port);
                        port = 6667;
                    }
                } else if (arg.equalsIgnoreCase("--channels") || arg.equalsIgnoreCase("-c")) {
                    chans = nextArg.split(",");
                }
            }
        } else {
            System.out.println("Incorrect argument count, exiting.");
            System.out.println("Usage: java FreeVoteBot -n nick -u user -r realname -h host -p port -c #channel,#list");
            System.exit(1);
        }
        try {
            final IrcConnection connection = new IrcConnection(serverHost, port);
            //nick in use listener, should be removed so client can handle this after
            final String finNick = nick;
            NumericListener nickInUse = new NumericListener() {
                public int getNumeric() {
                    return IrcConnection.ERR_NICKNAMEINUSE;
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
            connection.addListener(new PrivateMessageListener() {
                public void onPrivmsg(Privmsg privmsg) {
                    if (privmsg.getMessage().equals("!vote")) {
                        privmsg.getIrcConnection().sendMessage(privmsg.getTarget(), "Voting is not enabled yet");
                    }
                }
            });
            for (String channel : chans) {
                connection.joinChannel(channel);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
