package org.freecode.irc.votebot.api;

import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.KVStore;

public abstract class FVBModule implements Runnable {
    private volatile boolean enabled = true;

    public abstract boolean canRun(final Transmittable trns);

    public abstract void process(final Transmittable trns);

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

    public void run() {

    }

    private KVStore kvStore;
    private boolean kvLocal = false;

    public void setKvLocal(boolean kvLocal) {
        this.kvLocal = kvLocal;
    }

    public void setKvStore(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    public void store(String key, Object value) {
        kvStore.store(toClassKey(key), value);
    }

    public String readJson(String key) {
        return kvStore.readJson(key);
    }

    public <T> T read(String key, Class<T> classOfT) {
        return kvStore.read(toClassKey(key), classOfT);
    }

    public String readString(String key) {
        return read(key, String.class);
    }

    public Integer readInteger(String key) {
        return read(key, Integer.class);
    }

    private String toClassKey(String key) {
        if (kvLocal) return this.getClass().getSimpleName() + "." + key;
        else return "FreeVoteBot." + key;
    }

}
