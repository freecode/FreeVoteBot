package com.speed.irc.event.generators;

import com.speed.irc.connection.Server;
import com.speed.irc.event.EventGenerator;
import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.message.PrivateMessageEvent;
import com.speed.irc.types.*;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes PRIVMSG messages sent from the server.
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
public class PrivmsgGenerator implements EventGenerator {
    private static final Pattern PATTERN_PRIVMSG = Pattern
            .compile("(.+?)!(.+?)@(.+?) PRIVMSG (.+?) :(.*)");

    public boolean accept(RawMessage raw) {
        return PATTERN_PRIVMSG.matcher(raw.getRaw()).matches();
    }

    public IRCEvent generate(RawMessage raw) {
        final Matcher priv_matcher = PATTERN_PRIVMSG.matcher(raw.getRaw());
        final Server server = raw.getServer();
        if (priv_matcher.matches()) {
            final String msg = priv_matcher.group(5);
            final String sender = priv_matcher.group(1);
            final String user = priv_matcher.group(2);
            final String host = priv_matcher.group(3);
            final String name = priv_matcher.group(4);
            if (msg.startsWith("\u0001")) {// ctcp messages
                String request = msg.replace("\u0001", "");
                String reply = server.getCtcpReply(request);
                if (reply != null) {
                    server.sendRaw(String.format(
                            "NOTICE %s :\u0001%s %s\u0001\n", sender, request,
                            reply));
                }
            }
            Conversable conversable = null;
            if (Arrays.binarySearch(server.getChannelPrefix(), name.charAt(0)) >= 0) {
                conversable = server.getChannel(name);
                Channel c = (Channel) conversable;
                if (!c.isRunning()) {
                    c.setup();
                }
            } else {
                conversable = new ServerUser(sender, host, user, server);
            }
            return new PrivateMessageEvent(
                    new Privmsg(msg, sender, conversable), this);
        }
        return null;
    }

}
