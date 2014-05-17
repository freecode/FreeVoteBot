package com.speed.irc.types;

/**
 * Abstract class used to represent CTCP requests and replies, allows CTCP
 * replies to be dynamic.
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
public abstract class CTCPReply {
    /**
     * Gets the reply message
     *
     * @return the reply message
     */
    public abstract String getReply();

    /**
     * Gets the request message
     *
     * @return the request message
     */
    public abstract String getRequest();

    @Override
    public boolean equals(Object o) {
        if (o instanceof CTCPReply) {
            return getReply().equals(((CTCPReply) o).getReply())
                    && getRequest().equals(((CTCPReply) o).getRequest());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (getReply().hashCode() | getRequest().hashCode()) & 0xffffff;
    }
}
