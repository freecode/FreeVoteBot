package com.speed.irc.types;

/**
 * Represents a user in a channel.
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
public class ChannelUser extends ServerUser {
    private String modes;
    private ModeList channelModes;
    private final Channel channel;
    public static final int VOICE_FLAG = 0x1, HALF_OP_FLAG = 0x2,
            OP_FLAG = 0x4, ADMIN_FLAG = 0x8, OWNER_FLAG = 0x10;
    private int rights;

    public Channel getChannel() {
        return channel;
    }

    public ChannelUser(final String nick, final String modes,
                       final String user, final String host, final Channel channel) {
        super(nick, host, user, channel.getServer());
        this.channel = channel;
        this.channelModes = new ModeList(this.channel.server, "");
        this.modes = (modes);
        if (!modes.isEmpty())
            sync(modes);
    }

    public void sync(String modes) {
        channelModes.clear();
        StringBuilder builder = new StringBuilder("+");
        for (char c : modes.toCharArray()) {
            builder.append(channelModes.channelModeSymbolToLetter(c));
        }
        channelModes.parse(builder.toString());
        char[] modeSymbols = channel.getServer().getModeSymbols();
        if (modeSymbols.length == 5) {
            if (modes.indexOf(modeSymbols[0]) != -1)
                rights = rights | OWNER_FLAG;
            if (modes.indexOf(modeSymbols[1]) != -1)
                rights = rights | ADMIN_FLAG;
            if (modes.indexOf(modeSymbols[2]) != -1)
                rights = rights | OP_FLAG;
            if (modes.indexOf(modeSymbols[3]) != -1)
                rights = rights | HALF_OP_FLAG;
            if (modes.indexOf(modeSymbols[4]) != -1)
                rights = rights | VOICE_FLAG;
        } else if (modeSymbols.length == 2) {
            if (modes.indexOf(modeSymbols[0]) != -1)
                rights = rights | OP_FLAG;
            if (modes.indexOf(modeSymbols[1]) != -1)
                rights = rights | VOICE_FLAG;
        } else if (modeSymbols.length == 3) {
            if (modes.indexOf(modeSymbols[0]) != -1)
                rights = rights | OP_FLAG;
            if (modes.indexOf(modeSymbols[1]) != -1)
                rights = rights | HALF_OP_FLAG;
            if (modes.indexOf(modeSymbols[2]) != -1)
                rights = rights | VOICE_FLAG;
        } else if (modeSymbols.length == 4) {
            if (modes.indexOf(modeSymbols[0]) != -1)
                rights = rights | ADMIN_FLAG;
            if (modes.indexOf(modeSymbols[1]) != -1)
                rights = rights | OP_FLAG;
            if (modes.indexOf(modeSymbols[2]) != -1)
                rights = rights | HALF_OP_FLAG;
            if (modes.indexOf(modeSymbols[3]) != -1)
                rights = rights | VOICE_FLAG;
        }
    }

    public void addMode(char mode) {
        mode = channelModes.channelModeLetterToSymbol(mode);
        if (modes.indexOf(mode) < 0) {
            modes = modes + mode;
            sync(modes);
        }
    }

    public String getModes() {
        return modes;
    }

    public void removeExempts() {
        for (final Mask mask : channel.exempts) {
            if (mask.matches(this)) {
                channel.removeExempt(mask);
            }
        }
    }

    public void removeMode(char mode) {
        mode = channelModes.channelModeLetterToSymbol(mode);
        StringBuilder builder = new StringBuilder();
        for (char c : modes.toCharArray()) {
            if (c != mode) {
                builder.append(c);
            }
        }
        modes = builder.toString();
        sync(modes);
    }

    public boolean isOperator() {
        return (getRights() & OP_FLAG) != 0;
    }

    public boolean isHalfOperator() {
        return (getRights() & HALF_OP_FLAG) != 0;
    }

    public boolean isVoiced() {
        return (getRights() & VOICE_FLAG) != 0;
    }

    public boolean isOwner() {
        return (getRights() & OWNER_FLAG) != 0;
    }

    public boolean isProtected() {
        return (getRights() & ADMIN_FLAG) != 0;
    }

    /**
     * Useful if you're only checking for a single flag.
     *
     * @returns the bitmask of the user's flags
     */
    public int getRights() {
        return rights;
    }

    @Override
    public String toString() {
        return modes + getNick();
    }
}
