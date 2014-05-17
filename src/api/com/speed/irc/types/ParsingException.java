package com.speed.irc.types;

/**
 * Represents an exception being thrown while parsing.
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
public class ParsingException extends Exception {

    private static final long serialVersionUID = 2238835287734082797L;
    private final String msg;
    private final Exception e;

    /**
     * Initialises a ParsingException.
     *
     * @param msg the message
     * @param e   the exception
     */
    public ParsingException(final String msg, final Exception e) {
        this.msg = msg;
        this.e = e;

    }

    public void printStackTrace() {
        e.printStackTrace();
    }

    public String getMessage() {
        return msg;
    }

    /**
     * Gets the exception
     *
     * @return the exception.
     */
    public Exception getException() {
        return e;
    }

}
