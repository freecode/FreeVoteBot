package com.speed.irc.event;

/**
 * Allows identification as an event. All events must implement this class to be
 * dispatched by the event manager.
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
public interface IRCEvent {
    /**
     * Gets the object that dispatched this event
     *
     * @return the object that dispatched the event
     */
    public Object getSource();

    /**
     * Invokes the correct method(s) from the listener for this event.
     *
     * @param listener a listener object that accepts this event
     */
    public void callListener(IRCEventListener listener);
}
