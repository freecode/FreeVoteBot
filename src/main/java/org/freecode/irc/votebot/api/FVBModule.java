package org.freecode.irc.votebot.api;

import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.PropertyStore;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class FVBModule implements Runnable {

    private volatile boolean enabled = true;

    @Autowired
    private PropertyStore propertyStore;
    private boolean propertyLocal = false;

    public abstract boolean canRun(final Privmsg trns);

    public abstract void process(final Privmsg trns);

    public abstract String getName();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean b) {
        enabled = b;
    }

    public String toString() {
        return getName();
    }

    public void onConnect() {
    }

    public void run() {

    }

    public void setPropertyStore(PropertyStore propertyStore) {
        this.propertyStore = propertyStore;
    }

    public void setPropertyLocal(boolean propertyLocal) {
        this.propertyLocal = propertyLocal;
    }


    public void store(String key, Object value) {
        propertyStore.store(toKeypath(key), value);
    }

    public String getRawProperty(String key) {
        return propertyStore.getRawProperty(toKeypath(key));
    }

    public <T> T getProperty(String key, Class<T> classOfT) {
        return propertyStore.getProperty(toKeypath(key), classOfT);
    }

    public String getStringProperty(String key) {
        return getProperty(key, String.class);
    }

    public Integer getIntegerProperty(String key) {
        return getProperty(key, Integer.class);
    }

    public void clearProperty(String key) {
        propertyStore.clearProperty(key);
    }

    private String toKeypath(String key) {
        if (propertyLocal) return this.getClass().getSimpleName() + "." + key;
        else return "FreeVoteBot." + key;
    }

}
