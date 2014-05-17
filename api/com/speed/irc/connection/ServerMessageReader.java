package com.speed.irc.connection;

import com.speed.irc.event.api.ApiEvent;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Reads messages from the server and adds them to a queue. Encapsulates the
 * queue to prevent it being read and modified before the parser parses the
 * messages.
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
public class ServerMessageReader implements Runnable {
    private final Server server;
    private Queue<String> queue = new LinkedList<String>();
    private volatile String current;
    protected volatile boolean running = true;
    protected Logger logger = Logger.getLogger(Logger.class.getName());
    protected boolean logging;

    /**
     * No public access to queue to prevent reading before the parser. Gets the
     * next message to be read.
     *
     * @return the next message
     */
    protected String poll() {
        return queue.poll();
    }

    /**
     * Gets the next item on the queue without removing it from the queue.
     *
     * @return the next item on queue
     */
    public String peek() {
        return queue.peek();
    }

    /**
     * Checks to see if the queue is empty.
     *
     * @return true if they queue is empty, else false.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * No public access to queue to prevent reading before the parser. Gets the
     * queue
     *
     * @return the queue
     */
    protected Queue<String> getQueue() {
        return queue;
    }

    public ServerMessageReader(final Server server) {
        this.server = server;
    }

    public void run() {
        try {
            while (server.isConnected() && running
                    && (current = server.getReader().readLine()) != null) {
                try {
                    queue.add(current);
                } catch (IllegalStateException e) {

                    queue.clear();
                    queue.add(current);
                }
                if (logging) {
                    logger.info(current);
                }
                if (current.startsWith("ERROR :Closing Link:")) {
                    if (server.autoConnect && running) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        server.connect();
                        server.eventManager.dispatchEvent(new ApiEvent(
                                ApiEvent.SERVER_DISCONNECTED, server, this));
                        break;
                    }
                }
            }
        } catch (IOException e) {

            server.quit();

        }

    }

}
