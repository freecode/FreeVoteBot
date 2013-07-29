package org.freecode.irc.votebot;

import org.freecode.irc.*;
import org.freecode.irc.event.CtcpRequestListener;
import org.freecode.irc.event.NumericListener;
import org.freecode.irc.event.PrivateMessageListener;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Shivam
 * Date: 17/06/13
 * Time: 00:05
 */
public class FreeVoteBot implements PrivateMessageListener {

	private static final String CHANNEL = "#freecode";
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
							request.getNick(), "VERSION", "FreeVoteBot by " + CHANNEL + "on irc.rizon.net"));
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
		String[] chans = new String[]{CHANNEL};
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

	private void vote(final int nId, final int id, final Privmsg privmsg) {

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
					Pattern pattern = Pattern.compile("\u0002(.+?)\u0002");
					Matcher matcher = pattern.matcher(notice.getMessage());
					String mainNick = notice.getNick();
					if (matcher.find()) {
						mainNick = matcher.group(1);
					}
					ResultSet rs;
					PreparedStatement statement;
					statement = dbConn.prepareStatement("SELECT * FROM polls WHERE id = ?");
					statement.setInt(1, id);
					rs = statement.executeQuery();
					if (rs.next()) {
						statement = dbConn.prepareStatement("SELECT * FROM votes WHERE voter = ? AND pollId = ?");
						statement.setString(1, mainNick);
						statement.setInt(2, id);
						rs = statement.executeQuery();
						if (rs.next()) {
							if (rs.getInt("answerIndex") == nId) {
								privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "You've already voted with this option!", privmsg.getIrcConnection()));
							} else {
								PreparedStatement stmt = dbConn.prepareStatement("UPDATE votes SET answerIndex = ? WHERE voter = ? AND pollId = ?");
								stmt.setInt(1, nId);
								stmt.setString(2, mainNick);
								stmt.setInt(3, id);
								stmt.execute();
								privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Vote updated.", privmsg.getIrcConnection()));
							}
						} else {
							PreparedStatement stmt = dbConn.prepareStatement("INSERT INTO votes(pollId,voter,answerIndex) VALUES (?,?,?)");
							stmt.setInt(1, id);
							stmt.setString(2, mainNick);
							stmt.setInt(3, nId);
							stmt.execute();
							privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Vote cast.", privmsg.getIrcConnection()));

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				privmsg.getIrcConnection().removeListener(this);
			}
		});
		privmsg.getIrcConnection().send(new Privmsg("ChanServ", "WHY " + CHANNEL + " " + privmsg.getNick(), privmsg.getIrcConnection()));

	}

	public void onPrivmsg(final Privmsg privmsg) {
		if (privmsg.getMessage().toLowerCase().startsWith("!createpoll")) {
			final String msg = privmsg.getMessage().substring("!createpoll".length()).trim();
			if (msg.isEmpty() || msg.length() < 5) {
				privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Question is too short.", privmsg.getIrcConnection()));
				return;
			}
			privmsg.getIrcConnection().addListener(new NoticeFilter() {
				public boolean accept(Notice notice) {
					Pattern pattern = Pattern.compile("\u0002(.+?)\u0002");
					Matcher matcher = pattern.matcher(notice.getMessage());
					if (matcher.find() && matcher.find()) {
						String access = matcher.group(1);
						System.out.println(access);
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
						PreparedStatement statement = dbConn.prepareStatement("INSERT INTO polls(question) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
						statement.setString(1, msg.trim());
						statement.execute();
						ResultSet rs = statement.getGeneratedKeys();
						if (rs.next()) {
							int id = rs.getInt(1);
							privmsg.getIrcConnection().send(new Privmsg(privmsg.getTarget(), "Created poll, type !vote " + id + " yes/no/abstain to vote.", privmsg.getIrcConnection()));
						}
						privmsg.getIrcConnection().removeListener(this);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
			privmsg.getIrcConnection().send(new Privmsg("ChanServ", "WHY " + CHANNEL + " " + privmsg.getNick(), privmsg.getIrcConnection()));

		} else if (privmsg.getMessage().toLowerCase().startsWith("!v ") || privmsg.getMessage().toLowerCase().startsWith("!vote ")) {
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

		} else if (privmsg.getMessage().toLowerCase().startsWith("!msg") && privmsg.getNick().equals("Speed")) {
			String msg = privmsg.getMessage().substring(4).trim();
			String[] split = msg.split(" ", 2);
			String target = split[0];
			msg = split[1];
			privmsg.getIrcConnection().send(new Privmsg(target, msg, privmsg.getIrcConnection()));
		} else if (privmsg.getMessage().toLowerCase().startsWith("!y ")) {
			String id = privmsg.getMessage().replace("!y", "").trim();
			if (id.matches("\\d+")) {
				int i = Integer.parseInt(id);
				vote(0, i, privmsg);
			}
		} else if (privmsg.getMessage().toLowerCase().startsWith("!n ")) {
			String id = privmsg.getMessage().replace("!n", "").trim();
			if (id.matches("\\d+")) {
				int i = Integer.parseInt(id);
				vote(1, i, privmsg);
			}
		} else if (privmsg.getMessage().toLowerCase().startsWith("!a ")) {
			String id = privmsg.getMessage().replace("!a", "").trim();
			if (id.matches("\\d+")) {
				int i = Integer.parseInt(id);
				vote(2, i, privmsg);
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
					String str = users.substring(start, i == users.length() - 1 ? i + 1 : i);
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
