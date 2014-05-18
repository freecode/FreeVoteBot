package org.freecode.irc.votebot.api;

import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.KVStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;


public abstract class FVBModule implements Runnable {
    private volatile boolean enabled = true;

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

    public void onConnect(){};

    public void run() {

    }

    @Autowired
    private KVStore kvStore;
    private boolean kvLocal = false;

    public void setKvLocal(boolean kvLocal) {
        this.kvLocal = kvLocal;
    }

    public void setKvStore(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    public void store(String key, Object value) {
        kvStore.store(toKeypath(key), value);
    }

    public String readJson(String key) {
        return kvStore.readJson(toKeypath(key));
    }

    public <T> T read(String key, Class<T> classOfT) {
        return kvStore.read(toKeypath(key), classOfT);
    }

    public String readString(String key) {
        return read(key, String.class);
    }

    public Integer readInteger(String key) {
        return read(key, Integer.class);
    }

    public void remove(String key) {
        kvStore.remove(key);
    }

    private String toKeypath(String key) {
        if (kvLocal) return this.getClass().getSimpleName() + "." + key;
        else return "FreeVoteBot." + key;
    }

}
