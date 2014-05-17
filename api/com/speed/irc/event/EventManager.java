package com.speed.irc.event;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages events in a queue, and sends them to the appropriate listener.
 * Also manages a list of listeners.
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
public class EventManager implements Runnable {

    private List<IRCEventListener> listeners = new CopyOnWriteArrayList<IRCEventListener>();
    private BlockingQueue<IRCEvent> eventQueue = new LinkedBlockingQueue<IRCEvent>();

    /**
     * Adds an event to the event queue.
     *
     * @param event the event to be processed by the event queue.
     */
    public void dispatchEvent(final IRCEvent event) {
        eventQueue.offer(event);
    }

    /**
     * Adds an event listener to this event manager.
     *
     * @param listener the listener to be added to this event manager.
     */
    public void addListener(final IRCEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an event listener from this event manager.
     *
     * @param listener the listener to be removed
     * @return <tt>true</tt> if the listener was successfully removed,
     * <tt>false</tt> if it wasn't
     */
    public boolean removeListener(final IRCEventListener listener) {
        return listeners.remove(listener);
    }

    public void run() {
        IRCEvent e = eventQueue.poll();
        if (e != null) {
            for (IRCEventListener listener : listeners) {
                for (Class<?> clz : listener.getClass().getInterfaces()) {
                    if (clz.getAnnotation(ListenerProperties.class) != null) {
                        try {
                            ListenerProperties properties = clz.getAnnotation(ListenerProperties.class);
                            for (Class<? extends IRCEvent> clazz : properties.events()) {
                                if (e.getClass().isAssignableFrom(clazz)) {
                                    e.callListener(listener);
                                }
                            }
                        } catch (Exception e1) {
                            this.dispatchEvent(new com.speed.irc.event.api.ExceptionEvent(e1, this, null));
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Clears the queue of events to be processed.
     */
    public void clearQueue() {
        eventQueue.clear();

    }

}
