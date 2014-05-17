package com.speed.irc.types;

import com.speed.irc.connection.Server;
import com.speed.irc.event.channel.ChannelUserEvent;
import com.speed.irc.event.channel.ChannelUserListener;
import com.speed.irc.event.channel.ModeChangedEvent;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a channel
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
public class Channel extends Conversable implements ChannelUserListener,
        Runnable {
    protected String name;
    protected Server server;
    public volatile List<ChannelUser> users = new LinkedList<ChannelUser>();
    public volatile List<ChannelUser> userBuffer = new LinkedList<ChannelUser>();
    public volatile boolean isRunning = false;
    public long whoDelay = 120000L;
    private long topicSetTime;
    private String topicSetter;
    public int autoRejoinDelay = 50;
    protected boolean autoRejoin;
    public ModeList chanModeList;
    public List<Mask> bans = new LinkedList<Mask>();
    public List<Mask> exempts = new LinkedList<Mask>();
    public List<Mask> invites = new LinkedList<Mask>();
    protected String topic;
    protected ScheduledFuture<?> future;

    public Future<?> getFuture() {
        return future;
    }

    /**
     * Constructs a channel.
     *
     * @param name   the name of the channel.
     * @param server the server object this channel is associated with.
     */
    public Channel(final String name, final Server server) {
        this.name = name;
        this.server = server;
        this.server.getEventManager().addListener(this);
        this.server.addChannel(this);
        chanModeList = new ModeList(server, "");
    }

    /**
     * Gets the name of the channel.
     *
     * @return the name of the channel
     */
    public String getName() {
        return name;
    }

    public ModeList getModeList() {
        return chanModeList;
    }

    /**
     * Gets the list of users in the channel.
     *
     * @return The list of users in the channel.
     */
    public Collection<ChannelUser> getUsers() {
        return users;
    }

    /**
     * Gets a user from the channel.
     *
     * @param nick The nick of the ChannelUser to get.
     * @return The ChannelUser object associated with the nick or
     * <code>null</code>.
     */
    public ChannelUser getUser(final String nick) {
        for (ChannelUser user : users) {
            if (user.getNick().equalsIgnoreCase(nick)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Adds a channel user to this channel.
     *
     * @param user the user to add
     * @return <tt>true</tt> if it is added, <tt>false</tt> otherwise.
     */
    public boolean addChannelUser(final ChannelUser user) {
        return users.add(user);
    }

    /**
     * Removes a user from the channel.
     *
     * @param user the user to remove.
     * @return <tt>true</tt> if they were removed, <tt>false</tt> otherwise.
     */
    public boolean removeChannelUser(final ChannelUser user) {
        return users.remove(user);
    }

    /**
     * Checks whether the channel will be auto-rejoined when kicked.
     *
     * @return <tt>true</tt> if auto-rejoin is on, <tt>false</tt> otherwise.
     */
    public boolean isAutoRejoinOn() {
        return autoRejoin;
    }

    /**
     * Sets whether rejoining is enabled when kicked
     *
     * @param on turn auto-rejoin on or not
     */
    public void setAutoRejoin(final boolean on) {
        autoRejoin = on;
    }

    /**
     * Leaves the channel.
     *
     * @param message The part message, can be null for no message.
     */
    public void part(final String message) {
        isRunning = false;
        if (message != null && !message.isEmpty())
            server.sendRaw(String.format("PART %s :%s\n", name, message));
        else
            server.sendRaw(String.format("PART %s\n", name));
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void run() {
        if (isRunning) {
            server.sendRaw("WHO " + name);
            future = server.getChanExec().schedule(this, whoDelay,
                    TimeUnit.MILLISECONDS);
        }

    }

    /**
     * Gets the server the channel is on.
     *
     * @return the server the channel is on
     */
    public Server getServer() {
        return server;
    }

    /**
     * Joins the channel.
     */
    public void join() {
        server.sendRaw("JOIN :" + name);
    }

    public void setup() {
        server.sendRaw("MODE " + name);
        isRunning = true;
        if (!server.hasChannel(this))
            server.addChannel(this);
        future = server.getChanExec().schedule(this, 5000L,
                TimeUnit.MILLISECONDS);
    }

    public boolean isJoined() {
        return isRunning();
    }

    /**
     * Joins the channel using the provided password.
     *
     * @param password the password to join the channel with
     */
    public void join(final String password) {
        server.sendRaw("JOIN :" + name + " " + password);
        setup();
    }

    /**
     * Returns a sorted array of ChannelUser objects. This array is sorted by
     * first descending channel rank and then by descending alphabetical order
     * (by nick).
     *
     * @return the sorted array of users
     */
    public ChannelUser[] getSortedUsers() {
        final Collection<ChannelUser> users = getUsers();
        ChannelUser[] u = users.toArray(new ChannelUser[users.size()]);
        Arrays.sort(u, new Comparator<ChannelUser>() {

            public int compare(ChannelUser o1, ChannelUser o2) {
                int c = o2.getRights() - o1.getRights();
                if (c == 0) {
                    return o1.getNick().toLowerCase()
                            .compareTo(o2.getNick().toLowerCase());
                }
                return c;
            }
        });
        return u;
    }

    /**
     * Bans then kicks the channel user with the reason specified.
     *
     * @param user   the ChannelUser to kick.
     * @param reason The reason for kicking the channel user, can be
     *               <code>null</code>.
     */
    public void kickBan(final ChannelUser user, final String reason) {
        ban(user);
        kick(user, reason);
    }

    /**
     * Attempts to ban the specified ChannelUser.
     *
     * @param user the user that should be banned.
     */
    public void ban(final ChannelUser user) {
        final String banMask = "*!*@" + user.getHost();
        ban(banMask);
    }

    /**
     * Attempts to ban the specified mask.
     *
     * @param banMask The ban-mask that should be banned.
     */
    public void ban(final String banMask) {
        server.sendRaw(String.format("MODE %s +b %s\n", name, banMask));
    }

    /**
     * Attempts to kick a channel user.
     *
     * @param user   The ChannelUser that is to be kicked.
     * @param reason The reason for kicking the channel user, can be
     *               <code>null</code>.
     */
    public void kick(final ChannelUser user, String reason) {
        if (reason == null) {
            reason = user.getNick();
        }
        server.sendRaw(String.format("KICK %s %s :%s\n", name, user.getNick(),
                reason));
    }

    /**
     * Attempts to kick a channel user.
     *
     * @param nick   The nick of the user that is to be kicked.
     * @param reason The reason for kicking the channel user, can be
     *               <code>null</code>.
     */
    public void kick(final String nick, String reason) {
        final ChannelUser user = getUser(nick);
        if (user == null) {
            return;
        }
        if (reason == null) {
            reason = user.getNick();
        }
        server.sendRaw(String.format("KICK %s %s :%s\n", name, user.getNick(),
                reason));

    }

    /**
     * Sets the channel's topic. Attempts to send any changes to the server.
     *
     * @param topic The new channel topic.
     */
    public void sendTopic(final String topic) {
        server.sendRaw(String.format("TOPIC %s :%s\n", name, topic));
    }

    /**
     * Sets the topic in the memory.
     *
     * @param newTopic the new channel topic
     */
    public void setTopic(final String newTopic) {
        this.topic = newTopic;
    }

    /**
     * Gets the topic.
     *
     * @return the channel's topic
     */
    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Channel
                && ((Channel) o).getName().equalsIgnoreCase(getName());
    }

    public Collection<ChannelUser> getChannelUsers(final Mask mask) {
        final List<ChannelUser> users = new LinkedList<ChannelUser>();
        for (ChannelUser user : users) {
            if (mask.matches(user)) {
                users.add(user);
            }
        }
        return users;
    }

    public void channelUserJoined(ChannelUserEvent e) {
        if (e.getChannel().equals(this)) {
            if (getUser(e.getUser().getNick()) == null) {
                addChannelUser(e.getUser());
            } else {
                ChannelUser user = e.getChannel().getUser(e.getUser().getNick());
                user.user = e.getUser().getUser();
                user.host = e.getUser().getHost();
            }
            if (e.getUser().getNick().equals(server.getNick()) && !isRunning()) {
                setup();
            }
        }
    }

    public void channelUserParted(ChannelUserEvent e) {
        if (e.getChannel().equals(this)) {
            ChannelUser user = e.getUser();
            if (user != null) {
                removeChannelUser(user);
                if (user.getNick().equals(server.getNick())) {
                    isRunning = false;
                    future.cancel(true);
                }
            }

        }
    }

    public void channelUserModeChanged(ModeChangedEvent e) {
    }

    public void channelUserKicked(ChannelUserEvent e) {
        if (e.getChannel().equals(this)) {
            ChannelUser user = e.getUser();
            removeChannelUser(user);
            if (user.getNick().equals(server.getNick()) && isAutoRejoinOn()) {
                try {
                    Thread.sleep(autoRejoinDelay);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                isRunning = false;
                server.getChannels().remove(this);
                join();
            } else if (user.getNick().equals(server.getNick())) {
                isRunning = false;
                server.getChannels().remove(this);
            }
        }
    }

    /**
     * Sets a/many mode(s) on this channel. For example, to give a user voice
     * via modes:<br>
     * <tt>MODE #channel +vv-o nick1 nick2 nick2</tt> <br>
     * One would need to use: <br>
     * <tt>setMode("+vv-o", "nick1", "nick2", "nick2")</tt>
     *
     * @param mode the mode(s) to set.
     * @param args the arguments of the mode, for example nicknames.
     */
    public void setMode(String mode, String... args) {
        StringBuilder arg = new StringBuilder();
        for (String s : args) {
            arg.append(s).append(' ');
        }
        server.sendRaw(String.format("MODE %s %s %s", name, mode,
                arg.toString()));
    }

    /**
     * Removes kick exempt from a mask.
     *
     * @param mask the mask to remove the kick exempt from.
     */
    public void removeExempt(String mask) {
        setMode("-e", mask);
    }

    /**
     * Removes kick exempt from a mask.
     *
     * @param mask the mask to remove the kick exempt from.
     */
    public void removeExempt(Mask mask) {
        removeExempt(mask.toString());
    }

    public void channelUserNickChanged(ChannelUserEvent e) {
        final String newNick = e.getArgs()[1];
        if (e.getUser() != null) {
            final ChannelUser user = e.getUser();
            final ChannelUser replace = new ChannelUser(newNick,
                    user.getModes(), user.getUser(), user.getHost(),
                    e.getChannel());
            e.getChannel().removeChannelUser(user);
            e.getChannel().addChannelUser(replace);
        }
    }

    /**
     * @return the topic set time
     */
    public long getTopicSetTime() {
        return topicSetTime;
    }

    /**
     * sets the topic set time, should only really be used internally
     *
     * @param topicSetTime the topic set time
     */
    public void setTopicSetTime(long topicSetTime) {
        this.topicSetTime = topicSetTime;
    }

    /**
     * @return the topic setter
     */
    public String getTopicSetter() {
        return topicSetter;
    }

    /**
     * sets the topic setter, should only really be used internally
     *
     * @param topicSetter the topic setter
     */
    public void setTopicSetter(String topicSetter) {
        this.topicSetter = topicSetter;
    }

    public void channelUserQuit(ChannelUserEvent e) {
        if (e.getChannel().equals(this)) {
            ChannelUser user = e.getUser();
            removeChannelUser(user);
        }
    }
}