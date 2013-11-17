package org.freecode.irc.votebot.api;

import org.freecode.irc.Notice;
import org.freecode.irc.Privmsg;
import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.FreeVoteBot;
import org.freecode.irc.votebot.NoticeFilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AdminModule extends CommandModule {
    public AdminModule(FreeVoteBot fvb) {
        super(fvb);
    }

    @Override
    public void process(Transmittable trns) {
        final Privmsg privmsg = (Privmsg) trns;
        privmsg.getIrcConnection().addListener(new NoticeFilter() {
            public boolean accept(Notice notice) {
                Pattern pattern = Pattern.compile("\u0002(.+?)\u0002");
                Matcher matcher = pattern.matcher(notice.getMessage());
                if (matcher.find() && matcher.find()) {
                    String access = matcher.group(1);
                    System.out.println(access);
                    if (access.equals("Founder")) {
                        return notice.getNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains("\u0002" + privmsg.getNick() + "\u0002");
                    }
                }
                if (notice.getMessage().equals("Permission denied."))
                    notice.getIrcConnection().removeListener(this);
                return false;
            }

            public void run(Notice notice) {
                processMessage(privmsg);
                privmsg.getIrcConnection().removeListener(this);
            }
        });
        privmsg.getIrcConnection().send(new Privmsg("ChanServ", "WHY " + FreeVoteBot.CHANNEL + " " + privmsg.getNick(), privmsg.getIrcConnection()));
    }
}
