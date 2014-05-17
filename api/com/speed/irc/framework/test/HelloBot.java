package com.speed.irc.framework.test;

import com.speed.irc.event.api.ApiEvent;
import com.speed.irc.event.api.WhoisEvent;
import com.speed.irc.event.api.WhoisListener;
import com.speed.irc.event.channel.ChannelUserEvent;
import com.speed.irc.event.channel.ChannelUserListener;
import com.speed.irc.event.channel.ModeChangedEvent;
import com.speed.irc.event.message.PrivateMessageEvent;
import com.speed.irc.event.message.PrivateMessageListener;
import com.speed.irc.framework.Bot;
import com.speed.irc.types.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

/**
 * Greets people as they join the channel or speak a greeting.
 * <p/>
 * This file is part of Speed's IRC API.
 * <p/>
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * <p/>
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Shivam Mistry
 */
public class HelloBot extends Bot implements ChannelUserListener,
        PrivateMessageListener, WhoisListener {

    private static final String[] HELLO_PHRASES = new String[]{"Hello", "Hi",
            "Hey", "Yo", "Wassup", "helo", "herro", "hiya", "hai", "heya",
            "sup"};
    private static final Random RANDOM_GENERATOR = new Random();
    private volatile Channel[] channels;
    private final static String OWNER = "Speed";

    public HelloBot(final String server, final int port, final boolean ssl) {
        super(server, port, ssl);

    }

    @Override
    public String getRealName() {
        return "Hello Bot";
    }

    public static void main(String[] args) {
        new HelloBot("irc.rizon.net", 6697, true);
    }

    public Channel[] getChannels() {
        return channels;
    }

    public String getNick() {
        return "HelloBot";
    }

    public void onStart() {
        channels = new Channel[]{new Channel("#freecode", getServer())};
        channels[0].setAutoRejoin(true);
        // identify("password");
        getServer().setAutoReconnect(true);
        getServer().setReadDebug(true);
    }

    @Override
    public void apiEventReceived(final ApiEvent e) {
        super.apiEventReceived(e);
        if (e.getOpcode() == ApiEvent.SERVER_QUIT) {
            logger.info("WE HAVE QUIT FROM THE SERVER.");
        }
    }

    public void messageReceived(PrivateMessageEvent e) {
        final String message = e.getMessage().getMessage();
        final String sender = e.getMessage().getSender();
        if (message.contains("!raw") && sender.equals(OWNER)) {
            getServer().sendRaw(message.replaceFirst("!raw", "").trim());
        } else if (message.equals("!quit") && sender.equals(OWNER)) {
            getServer().quit("bai");
        } else if (message.equals("!list") && sender.equals(OWNER)) {
            Channel main = channels[0];
            if (main.isRunning()) {
                Collection<ChannelUser> users = main.getUsers();
                for (ChannelUser u : users) {
                    info(u.getMask().toString() + " - "
                            + Integer.toBinaryString(u.getRights()));
                }
            } else {
                info("Not in channel");
            }
        } else if (message.equals("!rejoin") && sender.equals(OWNER)) {
            channels[0].setAutoRejoin(!channels[0].isAutoRejoinOn());
            info(Boolean.toString(channels[0].isAutoRejoinOn()));
        } else if (message.startsWith("!verify") && sender.equals(OWNER)) {
            e.getMessage()
                    .getConversable()
                    .sendMessage(
                            Boolean.toString(Mask.verify(message.replaceFirst(
                                    "!verify", "").trim())));

        } else if (message.equals("!print")) {
            e.getMessage().getConversable().sendMessage(getServer().getNick());
        } else if (message.equals("!topic")) {
            if (e.getMessage().getConversable().isChannel()) {
                Channel c = e.getMessage().getConversable().getChannel();
                c.sendMessage("Topic: " + c.getTopic());
                c.sendMessage("Topic set by: " + c.getTopicSetter());
                c.sendMessage("Topic set at: "
                        + new Date(c.getTopicSetTime()).toString());
            }
        } else if (message.startsWith("!whois") && sender.equals(OWNER)) {
            String name = message.replace("!whois", "").trim();
            ServerUser user = getServer().getUser(name);
            user.requestWhois();
        }
        if (e.getMessage().getConversable() == null
                || !(e.getMessage().getConversable() instanceof Channel)) {
            return;
        }
        final Channel channel = (Channel) e.getMessage().getConversable();
        final ChannelUser user = channel.getUser(sender);
        for (String s : HELLO_PHRASES) {
            if (message.toLowerCase().equals(s.toLowerCase())
                    || (message.toLowerCase().contains(
                    getServer().getNick().toLowerCase()) && message
                    .toLowerCase().contains(s.toLowerCase()))) {
                channel.sendMessage(HELLO_PHRASES[RANDOM_GENERATOR
                        .nextInt(HELLO_PHRASES.length - 1)]
                        + " "
                        + sender
                        + " with rights: " + user.getRights());
            }

        }
    }

    public void channelUserJoined(ChannelUserEvent e) {
        e.getChannel().sendMessage(
                HELLO_PHRASES[RANDOM_GENERATOR
                        .nextInt(HELLO_PHRASES.length - 1)]
                        + " "
                        + e.getUser().getNick());
    }

    public void channelUserParted(ChannelUserEvent e) {
    }

    public void channelUserModeChanged(ModeChangedEvent e) {
    }


    public void channelUserKicked(ChannelUserEvent e) {
    }

    public void channelUserNickChanged(ChannelUserEvent e) {
        final String newNick = e.getArgs()[1];
        final String oldNick = e.getArgs()[0];
        info(oldNick + " changed to " + newNick);
        e.getChannel().sendMessage(
                HELLO_PHRASES[RANDOM_GENERATOR
                        .nextInt(HELLO_PHRASES.length - 1)] + " " + newNick);
    }

    public void whoisReceived(WhoisEvent e) {
        Whois is = e.getWhois();
        channels[0].sendMessage("Whois: " + is.getUser().toString());
        channels[0].sendMessage("Whois: " + Arrays.toString(is.getChannels()));
    }

    @Override
    public void channelUserQuit(ChannelUserEvent e) {
        // TODO Auto-generated method stub

    }

}
