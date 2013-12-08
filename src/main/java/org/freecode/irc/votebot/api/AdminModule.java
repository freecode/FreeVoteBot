package org.freecode.irc.votebot.api;

import org.freecode.irc.Notice;
import org.freecode.irc.Privmsg;
import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.FreeVoteBot;
import org.freecode.irc.votebot.NoticeFilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AdminModule extends CommandModule {


    private FreeVoteBot fvb;

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
	public final void process(Transmittable trns) {
		final Privmsg privmsg = (Privmsg) trns;
		privmsg.getIrcConnection().addListener(new NoticeFilter() {
			public boolean accept(Notice notice) {
				Pattern pattern = Pattern.compile("\u0002(.+?)\u0002");
				Matcher matcher = pattern.matcher(notice.getMessage());
				if (matcher.find() && matcher.find()) {
					String access = matcher.group(1);
					for (Right right : getRights()) {
						if (right.getCapitalisedName().equals(access)) {
							return notice.getNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains("\u0002" + privmsg.getNick() + "\u0002");
						}
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
		privmsg.getIrcConnection().send(new Privmsg("ChanServ", "WHY " + FreeVoteBot.CHANNEL_SOURCE + " " + privmsg.getNick(), privmsg.getIrcConnection()));
	}


    public final void setFvb(FreeVoteBot fvb) {
        this.fvb = fvb;
    }

    protected final FreeVoteBot getFvb() {
        return fvb;
    }
}
