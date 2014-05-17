package com.speed.irc.types;

import com.speed.irc.util.ControlCodeFormatter;

/**
 * Class used to encapsulate user masks
 * <p/>
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
public class Mask {

    private final String mask;

    /**
     * Initialise the user mask
     *
     * @param mask the mask to use
     */
    public Mask(final String mask) {
        this.mask = mask.toLowerCase();
        if (!verify(mask))
            throw new IllegalArgumentException("Mask doesn't match *!*@*: " + mask);
    }

    public Mask(final String nick, final String user, final String host) {
        this.mask = (nick + '!' + user + '@' + host).toLowerCase();
        if (!verify(mask))
            throw new IllegalArgumentException("Arguments are not valid: "
                    + mask);
    }

    /**
     * Verifies if the mask is valid
     *
     * @param mask the mask to check
     * @return <tt>true</tt> if the mask is valid, <tt>false</tt> if it isn't.
     */
    public static boolean verify(final String mask) {
        return mask
                .matches("[a-zA-Z\\*][\\-\\\\\\[\\]\\^\\`\\*\\w\\|]*?!~?[\\-\\\\\\[\\]\\^\\`\\*\\w\\|]+?@[\\-\\\\\\[\\]\\^\\`\\*\\w\\.\\:"
                        + ControlCodeFormatter.UNICODE_COLOUR + "]+");
    }

    /**
     * Checks if a user matches this mask.
     *
     * @param user the user to check
     * @return <tt>true</tt> if they do match, <tt>false</tt> if they don't
     */
    public boolean matches(ServerUser user) {
        String nickMask = mask.substring(0, mask.indexOf('!')).replace("*",
                ".*");
        String userMask = mask.substring(mask.indexOf('!') + 1,
                mask.indexOf('@')).replace("*", ".*");
        String hostMask = mask.substring(mask.indexOf('@') + 1)
                .replace("", "\\.").replace("*", ".*");
        return user.getNick().toLowerCase().matches(nickMask)
                && user.getUser().toLowerCase().matches(userMask)
                && user.getHost().toLowerCase().matches(hostMask);
    }

    public boolean equals(Object o) {
        return o instanceof Mask && ((Mask) o).mask.equals(mask);
    }

    public String toString() {
        return mask;
    }
}
