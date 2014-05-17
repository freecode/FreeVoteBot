package com.speed.irc.event.channel;

import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;
import com.speed.irc.types.ModeList;

/**
 * A class representing the change of modes in a Channel.
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
public class ModeChangedEvent extends ChannelEvent {
    private final ChannelUser affectedUser;
    private final ModeList modes;
    private final String affectedMask;
    private final String rawModes;

    public ModeChangedEvent(Channel channel, String senderNick, Object source, String... args) {
        super(channel, ChannelEvent.MODE_CHANGED, senderNick, source, args);
        modes = new ModeList(channel.getServer(), args[0]);
        affectedMask = args.length == 1 ? channel.getName() : args[1];
        affectedUser = null;
        rawModes = args[0];
    }

    public ModeChangedEvent(Channel channel, ChannelUser affectedUser, String senderNick, Object source, String... args) {
        super(channel, ChannelEvent.MODE_CHANGED, senderNick, source, args);
        this.affectedUser = affectedUser;
        this.modes = new ModeList(channel.getServer(), args[0]);
        affectedMask = null;
        rawModes = args[0];
    }

    /**
     * Gets the modes added to the channel
     *
     * @return the modes added to the channel
     */
    public ModeList getNewModes() {
        return modes;
    }

    /**
     * Gets the user affected by this mode change.
     *
     * @return the user affected by this change if appropriate, else null.
     */
    public ChannelUser getAffectedUser() {
        return affectedUser;
    }

    /**
     * Gets the mask affected by this mode change.
     *
     * @return the mask affected by this change if appropriate, else null.
     */
    public String getAffectedMask() {
        return affectedMask;
    }

    /**
     * Gets the raw mode affected by this mode change
     *
     * @return the mode affected by this change
     */
    public String getRowMode() {
        return rawModes;
    }

}
