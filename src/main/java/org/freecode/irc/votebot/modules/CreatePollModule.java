package org.freecode.irc.votebot.modules;

import org.freecode.irc.Notice;
import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.FreeVoteBot;
import org.freecode.irc.votebot.NoticeFilter;
import org.freecode.irc.votebot.api.CommandModule;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePollModule extends CommandModule {
    public CreatePollModule(FreeVoteBot fvb) {
        super(fvb);
    }

    @Override
    public void processMessage(final Privmsg privmsg) {
        if (privmsg.getMessage().trim().equals("!createpoll")) {
            return;
        }
        long txp = 604800 * 1000;
        final String msg;
        if (privmsg.getMessage().matches("!createpoll \\d{1,6}[whsdmWHSDM]? .+")) {
            final String[] parts = privmsg.getMessage().split(" ", 3);
            try {
                txp = parseExpiry(parts[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            msg = parts[2];
        } else {
            final String[] parts = privmsg.getMessage().split(" ", 2);
            msg = parts[1];
        }
        final long exp = System.currentTimeMillis() + txp;
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
                    if (access.equals("AOP") || access.equals("Founder") || access.equals("SOP") || access.equals("HOP")) {
                        return notice.getNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains("\u0002" + privmsg.getNick() + "\u0002");
                    }
                }
                if (notice.getMessage().equals("Permission denied."))
                    notice.getIrcConnection().removeListener(this);
                return false;
            }

            public void run(Notice notice) {
                try {
                    String mainNick = notice.getMessage().substring(notice.getMessage().indexOf("Main nick:") + 10).trim();
                    PreparedStatement statement = getFvb().getDbConn().prepareStatement("INSERT INTO polls(question, expiry, creator) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
                    statement.setString(1, msg.trim());
                    statement.setLong(2, exp);
                    statement.setString(3, mainNick);
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
        privmsg.getIrcConnection().send(new Privmsg("ChanServ", "WHY " + FreeVoteBot.CHANNEL + " " + privmsg.getNick(), privmsg.getIrcConnection()));
    }

    private long parseExpiry(String expiry) {
        if (expiry.matches("\\d{1,6}[whsdmWHSDM]?")) {
            long multiplier = 1000;
            if (Character.isLetter(expiry.charAt(expiry.length() - 1))) {
                char c = Character.toLowerCase(expiry.charAt(expiry.length() - 1));
                switch (c) {
                    case 'w':
                        multiplier *= 604800;
                        break;
                    case 'h':
                        multiplier *= 3600;
                        break;
                    case 'm':
                        multiplier *= 60;
                        break;
                    case 'd':
                        multiplier *= 86400;
                        break;
                    default:
                        break;
                }
                expiry = expiry.substring(0, expiry.length() - 1);
            }
            return Long.parseLong(expiry) * multiplier;
        } else {
            throw new IllegalArgumentException("too big");
        }
    }

    @Override
    public String getName() {
        return "createpoll";
    }


    @Override
    public String getParameterRegex() {
        return ".+";
    }
}
