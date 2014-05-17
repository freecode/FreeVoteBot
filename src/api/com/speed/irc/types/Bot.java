package com.speed.irc.types;

import com.speed.irc.connection.Server;
import com.speed.irc.event.IRCEventListener;
import com.speed.irc.event.api.ApiEvent;
import com.speed.irc.event.api.ApiListener;
import com.speed.irc.event.api.ExceptionEvent;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * The abstract class for making robots. To create a robot, you can extend this
 * class.
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
 * @deprecated use {@link com.speed.irc.framework.Bot} instead
 */
public abstract class Bot implements ApiListener {

    protected Server server;
    protected final int port;
    protected Logger logger = Logger.getLogger(Bot.class.getName());
    protected int modes;

    public int getPort() {
        return port;
    }

    public final void info(final String message) {
        logger.info(message);
    }

    public final Server getServer() {
        return server;
    }

    public abstract void onStart();

    public Bot(final String server, final int port) {
        this.port = port;
        try {
            this.server = new Server(new Socket(server, port));
            this.server.sendRaw("NICK " + getNick() + "\n");
            this.server.sendRaw("USER " + getUser() + " 0 * :" + getRealName());
            if (this instanceof IRCEventListener) {
                this.server.getEventManager().addListener(
                        (IRCEventListener) this);
            }
            onStart();
            for (Channel s : getChannels()) {
                s.join();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public abstract Channel[] getChannels();

    public abstract String getNick();

    public String getRealName() {
        return "SpeedsIrcApi";
    }

    public String getUser() {
        return "Speed";
    }

    private void connect() {
        this.server.sendRaw("NICK " + getNick() + "\n");
        this.server.sendRaw("USER " + getUser() + " " + modes + " * :"
                + getRealName() + "\n");
        for (Channel s : getChannels()) {
            s.join();
        }
    }

    /**
     * Used to identify to NickServ.
     *
     * @param password The password assigned to your nick
     */
    public void identify(final String password) {
        server.sendRaw("PRIVMSG NickServ :identify " + password + "\n");
    }

    public void apiEventReceived(ApiEvent e) {
        if (e.getOpcode() == ApiEvent.SERVER_DISCONNECTED) {
            connect();
        } else if (e.getOpcode() == ApiEvent.EXCEPTION_RECEIVED) {
            ((ExceptionEvent) e).getException().printStackTrace();
        }
    }
}
