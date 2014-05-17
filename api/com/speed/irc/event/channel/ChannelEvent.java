package com.speed.irc.event.channel;

import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.IRCEventListener;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ServerUser;

/**
 * Represents a channel event.
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
public abstract class ChannelEvent implements IRCEvent {

    /**
     * Constants for the ChannelEvent#getCode() method,
     * #TOPIC_CHANGED means the event was dispatched because the topic changed
     * #MODE_CHANGED means the event was dispatched because a channel mode was changed
     */
    public static final int TOPIC_CHANGED = 10,
            MODE_CHANGED = 11;
    private final int code;
    private final Channel channel;
    private String[] args;
    private final Object source;
    private ServerUser sender;

    public ChannelEvent(final Channel channel, final int code,
                        String senderNick, final Object source, final String... args) {
        this.code = code;
        this.channel = channel;
        this.source = source;
        this.sender = channel.getUser(senderNick);
        if (this.sender == null)
            // e.g. ChanServ etc
            this.sender = channel.getServer().getUser(senderNick);
        this.args = args;
    }

    public ChannelEvent(Channel channel2, int code2, Object source2,
                        final String... args) {
        this.code = code2;
        this.channel = channel2;
        this.source = source2;
        this.args = args;
    }

    /**
     * Gets the code of this event
     * See #TOPIC_CHANGED and #MODE_CHANGED
     *
     * @return the numeric code for this event
     * @see #TOPIC_CHANGED and #MODE_CHANGED
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the channel that this event was dispatched for
     *
     * @return The channel object that represents the channel this event was
     * dispatched for.
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Gets the user that caused this event
     *
     * @return The {@link ServerUser} (usually {@link com.speed.irc.types.ChannelUser})
     * that caused this event to be dispatched.
     */
    public ServerUser getSender() {
        return sender;
    }

    public Object getSource() {
        return source;
    }

    public void callListener(IRCEventListener listener) {
        if (listener instanceof ChannelUserListener
                && this instanceof ChannelUserEvent) {
            final ChannelUserListener l = (ChannelUserListener) listener;
            final ChannelUserEvent event = (ChannelUserEvent) this;
            switch (event.getCode()) {
                case ChannelUserEvent.USER_JOINED:
                    l.channelUserJoined(event);
                    break;
                case ChannelUserEvent.USER_KICKED:
                    l.channelUserKicked(event);
                    break;
                case ChannelUserEvent.USER_PARTED:
                    l.channelUserParted(event);
                    break;
                case ChannelUserEvent.USER_NICK_CHANGED:
                    l.channelUserNickChanged(event);
                    break;
                case ChannelUserEvent.USER_QUIT:
                    l.channelUserQuit(event);
                    break;
            }
        } else if (listener instanceof ChannelUserListener && this instanceof ModeChangedEvent) {
            final ChannelEvent event = this;
            final ChannelUserListener l = (ChannelUserListener) listener;
            final ModeChangedEvent mce = (ModeChangedEvent) this;
            if (event.getCode() == ChannelEvent.MODE_CHANGED && mce.getAffectedUser() != null) {
                l.channelUserModeChanged(mce);
            }
        } else if (listener instanceof ChannelEventListener) {
            final ChannelEventListener l = (ChannelEventListener) listener;
            final ChannelEvent event = this;
            switch (event.getCode()) {
                case ChannelEvent.MODE_CHANGED:
                    l.channelModeChanged((ModeChangedEvent) event);
                    break;
                case ChannelEvent.TOPIC_CHANGED:
                    l.channelTopicChanged((TopicChangedEvent) event);
                    break;
            }
        }
    }

    /**
     * Get the arguments of this event, usually used internally to parse meaningful
     * data.
     * <p/>
     * For {@link ModeChangedEvent} this should be the contents of this array:
     * <p/>
     * [0]: the mode including whether its + or -.
     * <p/>
     * [1]: the mask that was affected by this mode, if there was one at all
     * <p/>
     * For {@link TopicChangedEvent} this should be the contents of this array:
     * <p/>
     * [0]: the old topic
     * <p/>
     * [1]: the new topic
     *
     * @return the arguments of the event
     */
    public String[] getArgs() {
        return args;
    }
}
