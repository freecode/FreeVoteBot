package com.speed.irc.types;

import com.speed.irc.connection.Server;

/**
 * A representation of a user an a server.
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
public class ServerUser extends Conversable {
    protected String nick, host, user;
    private Server server;
    private boolean identified, away, oper;
    private String realName;

    /**
     * Initialises a server user.
     *
     * @param nick   the nick of the user
     * @param host   the host of the user
     * @param user   the username of the user
     * @param server the server the user is on
     */
    public ServerUser(final String nick, final String host, final String user,
                      final Server server) {
        this.nick = nick;
        this.host = host;
        this.user = user;
        this.server = server;
        getServer().addUser(this);
    }

    public String toString() {
        return String.format("%s!%s@%s", nick, user, host);
    }

    /**
     * Gets the mask of this user.
     *
     * @return the mask of the user
     */
    public Mask getMask() {
        return new Mask(getNick(),
                getUser() == null || getUser().isEmpty() ? "*" : getUser(),
                getHost() == null || getHost().isEmpty() ? "*" : getHost());
    }

    public void sendMessage(final String message) {
        server.sendRaw(String.format("PRIVMSG %s :%s", nick, message));
    }

    public void sendNotice(final String notice) {
        server.sendNotice(new Notice(notice, null, nick, server));
    }

    public String getName() {
        return nick;
    }

    /**
     * Gets the nick of this user.
     *
     * @return the nick of the user
     */
    public String getNick() {
        return nick;
    }

    /**
     * Gets the host of the user.
     *
     * @return the users host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the username of this user.
     *
     * @return the username of this user
     */
    public String getUser() {
        return user;
    }

    /**
     * Gets the server this user is on
     *
     * @return the server this user is on
     */
    public Server getServer() {
        return server;
    }

    /**
     * Returns whether the user is identified. Some servers will not send this
     * mode for users that are.
     *
     * @return whether the user is identified.
     */
    public boolean isIdentified() {
        return identified;
    }

    /**
     * Sets whether the user is identified.
     *
     * @param identified returns true if they are identified, false otherwise.
     */
    public void setIdentified(boolean identified) {
        this.identified = identified;
    }

    /**
     * Returns whether the user is away.
     *
     * @return whether the user is away.
     */
    public boolean isAway() {
        return away;
    }

    /**
     * Sets whether the user is away.
     *
     * @param away returns true if they are away, false otherwise.
     */
    public void setAway(boolean away) {
        this.away = away;
    }

    /**
     * Returns whether the user is a server operator.
     *
     * @return whether the user is a server operator.
     */
    public boolean isOper() {
        return oper;
    }

    /**
     * Sets whether the user is a server operator.
     *
     * @param oper returns true if they are an operator, false otherwise.
     */
    public void setOper(boolean oper) {
        this.oper = oper;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRealName() {
        return realName;
    }

    public boolean equals(final Object o) {
        if (!(o instanceof ServerUser))
            return false;
        else {
            ServerUser other = (ServerUser) o;

            return other != null && other.getNick().equalsIgnoreCase(nick);
        }
    }

    @Override
    public int hashCode() {
        return (getNick().hashCode() | getHost().hashCode() | getUser()
                .hashCode()) & 0xfffffff;
    }

    public void requestWhois() {
        getServer().sendRaw("WHOIS " + getName());
        getServer().addWhoisWaiting(this);
    }

}
