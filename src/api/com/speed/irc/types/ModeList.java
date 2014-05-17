package com.speed.irc.types;

import com.speed.irc.connection.Server;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A class representing user and channel modes.
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
public class ModeList {
    private Set<Character> modes = new CopyOnWriteArraySet<Character>();
    private final Server server;

    public ModeList(final Server server, final String modes) {
        this.server = server;
        if (!modes.isEmpty())
            parse(modes);
    }


    protected void clear() {
        modes.clear();
    }

    public char channelModeLetterToSymbol(char letter) {
        for (int i = 0; i < server.getModeLetters().length; i++) {
            if (server.getModeLetters()[i] == letter) {
                return server.getModeSymbols()[i];
            }
        }
        return '0';
    }

    public char channelModeSymbolToLetter(char symbol) {
        for (int i = 0; i < server.getModeSymbols().length; i++) {
            if (server.getModeSymbols()[i] == symbol) {
                return server.getModeLetters()[i];
            }
        }
        return '0';
    }

    public String parse() {
        if (modes.size() > 0) {
            StringBuilder builder = new StringBuilder(modes.size());
            for (char c : modes) {
                builder.append(c);
            }
            return '+' + builder.toString();
        } else {
            return "";
        }
    }

    public void parse(String modes) {
        boolean plus = false;
        for (int i = 0; i < modes.toCharArray().length; i++) {
            char c = modes.toCharArray()[i];
            if (c == '+') {
                plus = true;
                continue;
            } else if (c == '-') {
                plus = false;
                continue;
            }
            if (plus) {

                this.modes.add(c);
            } else {
                if (this.modes.contains(c))
                    this.modes.remove(c);

            }
        }
    }
}
