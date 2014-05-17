package com.speed.irc.event.generators;

import com.speed.irc.connection.Server;
import com.speed.irc.event.EventGenerator;
import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.api.WhoisEvent;
import com.speed.irc.types.RawMessage;
import com.speed.irc.types.Whois;
import com.speed.irc.util.Numerics;

import java.util.Collection;

/**
 * Generates WHOIS events.
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
public class WhoisGenerator implements EventGenerator {
    private final Server server;

    public WhoisGenerator(final Server server) {
        this.server = server;
    }

    public boolean accept(RawMessage raw) {
        for (String s : Numerics.WHOIS) {
            if (s.equals(raw.getCommand()))
                return true;
        }
        return false;
    }

    public IRCEvent generate(RawMessage raw) {
        String user = raw.getRaw().split(" ")[3];
        Collection<RawMessage> messages = server.whoisWaiting.get(server
                .getUser(user));
        if (raw.getCommand().equals(Numerics.WHOIS_END)) {
            return new WhoisEvent(new Whois(messages, server), this);
        } else {
            messages.add(raw);
            return null;
        }
    }

}
