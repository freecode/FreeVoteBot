package org.freecode.irc.votebot.entity;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/21/13
 * Time: 7:29 PM
 */
public class Poll {
    private int id;
    private long expiry;
    private String question, options, creator;
    private boolean closed;

    public int getId() {
        return id;
    }

    public long getExpiry() {
        return expiry;
    }

    public String getQuestion() {
        return question;
    }

    public String getOptions() {
        return options;
    }

    public String getCreator() {
        return creator;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
