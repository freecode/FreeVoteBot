package org.freecode.irc.votebot;

import com.google.gson.Gson;
import org.freecode.irc.Privmsg;
import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.FreeVoteBot;
import org.freecode.irc.votebot.api.FVBModule;

/**
 * Created with IntelliJ IDEA.
 * User: shivam
 * Date: 10/28/13
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestModule extends FVBModule {

    public TestModule(FreeVoteBot fvb) {
        super(fvb);
    }

    @Override
    public boolean canRun(Transmittable trns) {
        return trns instanceof Privmsg && ((Privmsg) trns).getMessage().equalsIgnoreCase("!test");
    }

    @Override
    public void process(Transmittable trns) {
        Privmsg privmsg = (Privmsg) trns;
        String lastSender;
        if ((lastSender = getProperty(this.getClass(), "sender.last")) == null) {
            privmsg.send("Successful test!");
        } else {
            //lastSender = new Gson().fromJson(lastSender, String.class);
            privmsg.send("Successful test! Last tester was: " + lastSender);
        }
        storeProperty(this.getClass(), "sender.last", privmsg.getNick());
    }

    public String getName() {
        return "testmod";
    }
}
