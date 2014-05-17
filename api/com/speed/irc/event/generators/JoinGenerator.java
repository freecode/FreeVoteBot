package com.speed.irc.event.generators;

import com.speed.irc.event.EventGenerator;
import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.channel.ChannelUserEvent;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;
import com.speed.irc.types.RawMessage;

/**
 * Processes JOIN messages sent from the server.
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
public class JoinGenerator implements EventGenerator {

    public boolean accept(RawMessage raw) {
        return raw.getCommand().equals("JOIN");
    }

    public IRCEvent generate(RawMessage raw) {
        final String[] parts = raw.getRaw().split("!");
        final String nick = parts[0];
        final String user = parts[1].split("@")[0];
        final String host = parts[1].split("@")[1].split(" ")[0];
        String chan = raw.getRaw().split(" ")[2];
        if (raw.getRaw().split(" ")[2].startsWith(":")) {
            chan = chan.substring(1);
        }
        Channel channel = raw.getServer().getChannel(chan);
        if (channel == null) {
            channel = new Channel(chan, raw.getServer());
            channel.setup();
        } else if (!channel.isRunning()) {
            channel.setup();
        }
        if (channel.getUser(nick) != null) {
            channel.removeChannelUser(channel.getUser(nick));
        }
        final ChannelUser u = new ChannelUser(nick, "", user, host, channel);
        return new ChannelUserEvent(this, channel, u,
                ChannelUserEvent.USER_JOINED);
    }

}
