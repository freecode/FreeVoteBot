package com.speed.irc.event.generators;

import com.speed.irc.event.EventGenerator;
import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.channel.ChannelUserEvent;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;
import com.speed.irc.types.RawMessage;

/**
 * Processes PART messages sent from the server.
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
public class PartGenerator implements EventGenerator {

    public boolean accept(RawMessage raw) {
        return raw.getCommand().equals("PART");
    }

    public IRCEvent generate(RawMessage raw) {
        final String nick = raw.getSender().split("!")[0];
        Channel channel = raw.getServer()
                .getChannel(raw.getRaw().split(" ")[2]);
        if (!channel.isRunning()) {
            channel.setup();
        }
        final ChannelUser user = channel.getUser(nick);
        String[] parts = raw.getRaw().split(" :", 2);
        String partMsg = "";
        if (parts.length > 1) {
            partMsg = parts[1];
        }
        return new ChannelUserEvent(this, channel, user,
                ChannelUserEvent.USER_PARTED, partMsg);
    }

}
