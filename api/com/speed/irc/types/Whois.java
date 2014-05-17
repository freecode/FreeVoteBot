package com.speed.irc.types;

import com.speed.irc.connection.Server;
import com.speed.irc.util.Numerics;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a collection of WHOIS replies.
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
public class Whois {
    private Collection<RawMessage> whois;
    private Channel[] channels;
    private ServerUser user;
    private Server server;

    public Whois(final Collection<RawMessage> whois, final Server server) {
        this.whois = whois;
        this.server = server;
        parse();

    }

    public ServerUser getUser() {
        return user;
    }

    public Channel[] getChannels() {
        return channels;
    }

    public Server getServer() {
        return server;
    }

    private void parse() {
        for (RawMessage m : whois) {
            if (user == null) {
                user = server.getUser(m.getRaw().split(" ")[3]);
            }
            if (m.getCommand().equals(Numerics.WHOIS_CHANNELS)) {
                String[] channels = m.getRaw().split(" :", 2)[1].split(" ");
                this.channels = new Channel[channels.length];
                for (int i = 0; i < channels.length; i++) {
                    String name = channels[i];
                    String mode = "";
                    if (name.charAt(0) != '#') {
                        mode += name.charAt(0);
                        name = name.substring(1);
                    }
                    Channel c = new Channel(name, server);
                    if (c.getUser(user.getNick()) == null) {
                        c.addChannelUser(new ChannelUser(user.getNick(), mode,
                                user.getUser(), user.getHost(), c));
                    } else {
                        ChannelUser cu = c.getUser(user.getNick());
                        cu.addMode(mode.charAt(0));
                    }
                    this.channels[i] = c;
                }
            } else if (m.getCommand().equals(Numerics.WHOIS_NAME)) {
                String msg = m.getRaw();
                String[] parts = msg.split(" ");
                String nick = parts[3];
                String user = parts[4];
                String host = parts[5];
                String realName = msg.split(" :", 2)[1];
                this.user = new ServerUser(nick, host, user, server);
                this.user.setRealName(realName);
            }
        }
    }

    public Collection<RawMessage> getMessages() {
        return Collections.unmodifiableCollection(whois);
    }

}
