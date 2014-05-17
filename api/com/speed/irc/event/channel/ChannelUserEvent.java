package com.speed.irc.event.channel;

import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;

/**
 * Represents a channel user event.
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
public class ChannelUserEvent extends ChannelEvent {

    private ChannelUser user;
    public static final int USER_JOINED = 0, USER_PARTED = 1, USER_MODE_CHANGED = 2, USER_KICKED = 3,
            USER_NICK_CHANGED = 4, USER_QUIT = 5;

    public ChannelUserEvent(Object source, final Channel channel, final ChannelUser user, final int code) {
        super(channel, code, source);
        this.user = user;
    }

    public ChannelUserEvent(Object source, final Channel channel, final ChannelUser user, final String sender,
                            final int code, final String... args) {
        super(channel, code, sender, source, args);
        this.user = user;
    }

    public ChannelUserEvent(Object source, final Channel channel, final ChannelUser user, final int code,
                            final String... args) {
        super(channel, code, source, args);
        this.user = user;
    }

    public ChannelUser getUser() {
        return user;
    }

    /**
     * @return
     * @deprecated see {@link #getArgs()} instead
     */
    public String[] getArguments() {
        return getArgs();
    }

}
