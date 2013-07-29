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
import java.util.ArrayList;
import java.util.Arrays;

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
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS polls (id integer PRIMARY KEY AUTOINCREMENT, question string NOT NULL, options string NOT NULL DEFAULT 'yes,no,abstain')");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS votes (pollId integer, voter string NOT NULL, answerIndex integer NOT NULL)");

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
				PreparedStatement statement = dbConn.prepareStatement("INSERT INTO polls(question) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
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
				String ids = split[0];
				String vote = split[1].toLowerCase();
				if (!vote.equalsIgnoreCase("yes") && !vote.equalsIgnoreCase("no") && !vote.equalsIgnoreCase("abstain")) {
					return;
				}
				int nId;
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
				int id = Integer.parseInt(ids);
				try {
					ResultSet rs;
					PreparedStatement statement;
					statement = dbConn.prepareStatement("SELECT * FROM polls WHERE id = ?");
					statement.setInt(1, id);
					rs = statement.executeQuery();
					if (rs.next()) {
						statement = dbConn.prepareStatement("SELECT * FROM votes WHERE voter = ? AND pollId = ?");
						statement.setString(1, privmsg.getNick());
						statement.setInt(2, id);
						rs = statement.executeQuery();
						if (rs.next()) {
							PreparedStatement stmt = dbConn.prepareStatement("UPDATE votes SET answerIndex = ? WHERE voter = ? AND pollId = ?");
							stmt.setInt(1, nId);
							stmt.setString(2, privmsg.getNick());
							stmt.setInt(3, id);
							stmt.execute();
							privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Vote updated", privmsg.getIrcConnection()));
						} else {
							PreparedStatement stmt = dbConn.prepareStatement("INSERT INTO votes(pollId,voter,answerIndex) VALUES (?,?,?)");
							stmt.setInt(1, id);
							stmt.setString(2, privmsg.getNick());
							stmt.setInt(3, nId);
							stmt.execute();
							privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Vote cast", privmsg.getIrcConnection()));

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (split.length == 1) {
				String id = split[0];
				if (!id.matches("\\d+")) {
					return;
				}
				try {
					ResultSet rs;
					PreparedStatement statement;
					statement = dbConn.prepareStatement("SELECT * FROM polls WHERE id = ?");
					statement.setInt(1, Integer.parseInt(id));
					rs = statement.executeQuery();
					String question = null;
					String[] options = null;
					if (rs.next()) {
						question = rs.getString("question");
						options = stringToArray(rs.getString("options"));
					}
					if (question != null) {
						statement = dbConn.prepareStatement("SELECT * FROM votes WHERE pollId = ?");
						statement.setInt(1, Integer.parseInt(id));
						rs = statement.executeQuery();
						int yes = 0, no = 0, abstain = 0;
						while (rs.next()) {
							int i = rs.getInt("answerIndex");
							if (i == 0) {
								yes++;
							} else if (i == 1) {
								no++;
							} else if (i == 2) {
								abstain++;
							}
						}
						privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Poll: " + question +
								" Options: " + Arrays.toString(options) + " Yes: " + yes + " No: " + no + " Abstain: "
								+ abstain, privmsg.getIrcConnection()));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	private String[] stringToArray(String users) {
		if (users == null || users.length() == 0)
			return new String[0];
		ArrayList<String> voters = new ArrayList<String>();
		int start = 0;
		for (int i = 1; i < users.length(); i++) {
			char c = users.charAt(i);
			if (c == ',' || i == users.length() - 1) {
				if (i - 1 > 0 && users.charAt(i - 1) == '\\' && i != users.length() - 1) {
					continue;
				} else {
					String str = users.substring(start, i == users.length() - 1 ? i + 1 : i );
					voters.add(str);
					start = i + 1;
				}
			}
		}
		return voters.toArray(new String[voters.size()]);
	}

	public String arrayToString(String[] users) {
		StringBuilder builder = new StringBuilder();
		for (String s : users) {
			builder.append(s).append(',');
		}
		return builder.substring(0, builder.lastIndexOf(","));
	}
}
