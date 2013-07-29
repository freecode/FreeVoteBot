package org.freecode.irc.votebot;

import org.freecode.irc.CtcpRequest;
import org.freecode.irc.CtcpResponse;
import org.freecode.irc.IrcConnection;
import org.freecode.irc.Privmsg;
import org.freecode.irc.event.CtcpRequestListener;
import org.freecode.irc.event.NumericListener;
import org.freecode.irc.event.PrivateMessageListener;

import java.io.IOException;
import java.sql.*;

/**
 * User: Shivam
 * Date: 17/06/13
 * Time: 00:05
 */
public class FreeVoteBot implements PrivateMessageListener {

	private String nick, realName, serverHost, user;
	private int port;
	private IrcConnection connection;
	private Connection dbConn;

	public FreeVoteBot(String nick, String user, String realName, String serverHost, String[] chans, int port) {
		try {
			connection = new IrcConnection(serverHost, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Class.forName("org.sqlite.JDBC");
			dbConn = DriverManager.getConnection("jdbc:sqlite:freecode.db");
			Statement statement = dbConn.createStatement();
			statement.setQueryTimeout(5);
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS poll (id integer PRIMARY KEY AUTOINCREMENT, question " +
					"string NOT NULL, yesVotes integer DEFAULT 0, noVotes integer DEFAULT 0, abstainVotes integer DEFAULT 0)");

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		this.nick = nick;
		this.realName = realName;
		this.serverHost = serverHost;
		this.port = port;
		this.user = user;
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
		try {
			connection.register(nick, user, realName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		connection.removeListener(nickInUse);
		connection.addListener(this);
		connection.addListener(new CtcpRequestListener() {

			public void onCtcpRequest(CtcpRequest request) {
				if (request.getCommand().equals("VERSION")) {
					request.getIrcConnection().send(new CtcpResponse(request.getIrcConnection(),
							request.getNick(), "VERSION", "FreeVoteBot by #freecode on irc.rizon.net"));
				} else if (request.getCommand().equals("PING")) {
					request.getIrcConnection().send(new CtcpResponse(request.getIrcConnection(),
							request.getNick(), "PING", request.getArguments()));
				}
			}
		});
		for (String channel : chans) {
			connection.joinChannel(channel);

		}
	}

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
			new FreeVoteBot(nick, user, realName, serverHost, chans, port);
		} else {
			System.out.println("Incorrect argument count, exiting.");
			System.out.println("Usage: java FreeVoteBot -n nick -u user -r realname -h host -p port -c #channel,#list");
			System.exit(1);
		}

	}

	public void onPrivmsg(Privmsg privmsg) {
		if (privmsg.getMessage().startsWith("!createpoll")) {
			String msg = privmsg.getMessage().substring("!createpoll".length()).trim();
			try {
				PreparedStatement statement = dbConn.prepareStatement("INSERT INTO poll(question) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, msg.trim());
				statement.execute();
				ResultSet rs = statement.getGeneratedKeys();
				if (rs.next()) {
					int id = rs.getInt(1);
					privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Created poll, type !vote " + id + " yes/no/abstain to vote.", privmsg.getIrcConnection()));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (privmsg.getMessage().startsWith("!vote")) {
			String msg = privmsg.getMessage().substring("!vote".length()).trim();
			System.out.println(msg);
			String[] split = msg.split(" ", 2);
			if (split.length == 2) {
				String id = split[0];
				String vote = split[1].toLowerCase();
				if (!vote.equalsIgnoreCase("yes") && !vote.equalsIgnoreCase("no") && !vote.equalsIgnoreCase("abstain")) {
					return;
				}
				if (!id.matches("\\d+")) {
					return;
				}
				try {
					PreparedStatement statement = dbConn.prepareStatement("SELECT * FROM poll WHERE id = ?");
					statement.setInt(1, Integer.parseInt(id));

					ResultSet rs = statement.executeQuery();
					if (rs.next()) {
						PreparedStatement stmt = null;
						int yes = rs.getInt("yesVotes"), no = rs.getInt("noVotes"), abstain = rs.getInt("abstainVotes");
						if (vote.equals("yes")) {
							yes++;
							stmt = dbConn.prepareStatement("UPDATE poll SET yesVotes = ? WHERE id = ?");
							stmt.setInt(1, yes);
							stmt.setInt(2, Integer.parseInt(id));
						} else if (vote.equals("no")) {
							no++;
							stmt = dbConn.prepareStatement("UPDATE poll SET noVotes = ? WHERE id = ?");
							stmt.setInt(1, no);
							stmt.setInt(2, Integer.parseInt(id));
						} else {
							abstain++;
							stmt = dbConn.prepareStatement("UPDATE poll SET abstainVotes = ? WHERE id = ?");
							stmt.setInt(1, abstain);
							stmt.setInt(2, Integer.parseInt(id));
						}
						stmt.execute();

						privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Vote cast for "
								+ rs.getString("question") + ", current stats: yes: " + yes + " no: " + no +
								" abstain: " + abstain, privmsg.getIrcConnection()));


					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}
}
