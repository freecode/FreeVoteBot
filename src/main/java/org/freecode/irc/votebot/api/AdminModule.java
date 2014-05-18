package org.freecode.irc.votebot.api;

import com.speed.irc.types.Notice;
import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.FreeVoteBot;
import org.freecode.irc.votebot.NoticeFilter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AdminModule extends CommandModule {

    @Autowired
    protected FreeVoteBot fvb;

    protected enum Right {
        FOUNDER("Founder"), AOP("AOP"), SOP("SOP"), HOP("HOP"), VOP("VOP");
        private final String capitalisedName;

        Right(String capitalisedName) {
            this.capitalisedName = capitalisedName;
        }

        public String getCapitalisedName() {
            return capitalisedName;
        }
    }

    protected abstract Right[] getRights();

    @Override
    public final void process(final Privmsg privmsg) {
        privmsg.getConversable().getServer().getEventManager().addListener(new NoticeFilter() {
            public boolean accept(Notice notice) {
                Pattern pattern = Pattern.compile("\u0002(.+?)\u0002");
                Matcher matcher = pattern.matcher(notice.getMessage());
                if (matcher.find() && matcher.find()) {
                    String access = matcher.group(1);
                    for (Right right : getRights()) {
                        if (right.getCapitalisedName().equals(access)) {
                            return notice.getSenderNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains("\u0002" + privmsg.getSender() + "\u0002");
                        }
                    }
                }
                if (notice.getMessage().equals("Permission denied."))
                    privmsg.getConversable().getServer().getEventManager().removeListener(this);
                return false;
            }

            public void run(Notice notice) {
                processMessage(privmsg);
                privmsg.getConversable().getServer().getEventManager().removeListener(this);
            }
        });

        askChanServForUserCreds(privmsg);
    }


    public final void setFvb(FreeVoteBot fvb) {
        this.fvb = fvb;
    }

    protected final FreeVoteBot getFvb() {
        return fvb;
    }
}
