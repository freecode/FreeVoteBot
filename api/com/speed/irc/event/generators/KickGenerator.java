package com.speed.irc.event.generators;

import com.speed.irc.event.EventGenerator;
import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.channel.ChannelUserEvent;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;
import com.speed.irc.types.RawMessage;

/**
 * Processes KICK messages sent from the server.
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
public class KickGenerator implements EventGenerator {

    public boolean accept(RawMessage raw) {
        return raw.getCommand().equals("KICK");
    }

    public IRCEvent generate(RawMessage raw) {
        final Channel channel = raw.getServer().getChannel(
                raw.getRaw().split(" ")[2]);
        if (channel == null) {
            return null;
        }
        final ChannelUser user = channel.getUser(raw.getRaw().split(" ")[3]);
        if (user == null) {
            return null;
        }
        String kickMsg = "";
        String[] parts = raw.getRaw().split(" :", 2);
        if (parts.length > 1) {
            kickMsg = parts[1];
        }
        return new ChannelUserEvent(this, channel, user, raw.getSender().split(
                "!")[0].trim(), ChannelUserEvent.USER_KICKED, kickMsg);
    }
}
