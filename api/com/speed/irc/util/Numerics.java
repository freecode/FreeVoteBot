package com.speed.irc.util;

/**
 * Stores IRC numerics used by the API internal classes. Numerics are stored as
 * strings to allow easy comparison. (numerics are parsed as strings)
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
public interface Numerics {
    String WHO_RESPONSE = "352";
    String WHO_END = "315";
    String CHANNEL_NAMES = "353";
    String CHANNEL_TOPIC = "332", CHANNEL_TOPIC_SET = "333";
    String CHANNEL_NAMES_END = "366";
    String SERVER_SUPPORT = "005";
    String NOT_AN_OPERATOR = "482";
    String CHANNEL_MODES = "324";
    String CHANNEL_IS_FULL = "471", INVITE_ONLY_CHANNEL = "473",
            BANNED_FROM_CHANNEL = "474", BAD_CHANNEL_KEY = "475",
            BAD_CHANNEL_MASK = "476";
    // whois numerics
    String WHOIS_NAME = "311", WHOIS_CHANNELS = "319", WHOIS_SERVER = "312",
            WHOIS_OPERATOR = "313", WHOIS_IDLE = "317";
    String WHOIS_END = "318";
    String[] WHOIS = new String[]{WHOIS_NAME, WHOIS_CHANNELS, WHOIS_SERVER,
            WHOIS_OPERATOR, WHOIS_IDLE, WHOIS_END};
}
