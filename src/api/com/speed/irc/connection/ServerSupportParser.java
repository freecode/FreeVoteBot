package com.speed.irc.connection;

import com.speed.irc.types.RawMessage;
import com.speed.irc.util.Numerics;

import java.util.Properties;

/**
 * Parses the server support message (numeric 005)
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
public class ServerSupportParser {

    private RawMessage[] messages;
    private int index;
    private Properties settings = new Properties();

    private static final String CHANTYPES = "CHANTYPES";

    public ServerSupportParser() {
        this.messages = new RawMessage[0];
        index = 0;
    }

    private void addMessage(RawMessage message) {
        if (!message.getCommand().equals(Numerics.SERVER_SUPPORT))
            throw new IllegalArgumentException("Wrong numeric: "
                    + message.getCommand());
        RawMessage[] msgs = new RawMessage[index + 1];
        msgs[index] = message;
        for (int i = 0; i < index; i++) {
            msgs[i] = messages[i];
        }
        index++;
        messages = msgs;
    }

    public void parse(RawMessage msg) {
        addMessage(msg);
        String message = msg.getRaw();
        if (msg.getRaw().contains(" :are supported by this server")) {
            message = msg.getRaw()
                    .replace(" :are supported by this server", "").trim();

        }
        String[] parts = message.split(" ");
        for (String s : parts) {
            String key = null;
            String value = null;
            if (s.contains("=")) {
                String[] t = s.split("=", 2);
                key = t[0];
                value = t[1];
            } else {
                key = s;
                value = s;
            }
            settings.put(key, value);
        }
    }

    public Properties getSettings() {
        return settings;
    }

    public char[] getChanTypes() {
        if (!getSettings().containsKey(CHANTYPES)) {
            return new char[]{'@'};
        } else {
            return settings.getProperty(CHANTYPES).toCharArray();
        }
    }

    public char[][] getChanModes() {
        String s = getSettings().getProperty("CHANMODES");
        String[] modes = s.split(",", 4);
        return new char[][]{modes[0].toCharArray(), modes[1].toCharArray(),
                modes[2].toCharArray(), modes[3].toCharArray()};
    }
}
