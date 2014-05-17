package com.speed.irc.types;

import com.speed.irc.connection.Server;

/**
 * A class representing a single user and channel mode.
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
public class Mode {

    private final char mode;
    private final boolean plus;

    public Mode(final String s) {
        if (!s.matches("[\\+-][A-Za-z]")) {
            throw new IllegalArgumentException("Incorrect mode format");
        }
        this.mode = s.charAt(1);
        this.plus = s.charAt(0) == '+';
    }

    public Mode(final boolean plus, final char mode) {
        this.plus = plus;
        this.mode = mode;
    }

    public ModeList newModeList(final Server server) {
        return new ModeList(server, toString());
    }


    public String toString() {
        return String.valueOf(plus ? '+' : '-') + mode;
    }
}
