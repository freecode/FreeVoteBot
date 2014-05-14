package org.freecode.irc.votebot.api;

import com.google.gson.Gson;
import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.KVStore;

import java.util.Properties;

public abstract class FVBModule implements Runnable {
    private volatile boolean enabled = true;
    protected static Properties properties = new Properties();

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
    public void setKvStore(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    public void store(String key, Object value) {
        kvStore.store(toClassKey(key), value);
    }

    public String readString(String key) {
        return read(key, String.class);
    }

    public String readJson(String key) {
        return read(key, String.class);
    }

    public <T> T read(String key, Class<T> classOfT) {
        return kvStore.read(toClassKey(key), classOfT);
    }

    private String toClassKey(String key) {
        return this.getClass().getCanonicalName() + "." + key;
    }

    public static String getProperty(final String className, final String property) {
        Object val;
        return (val = properties.get(className + "#" + property)) == null ? null : val.toString();
    }

    public static String getProperty(final Class clazz, final String property) {
        return getProperty(clazz.getName(), property);
    }

    public static void storeProperty(final Class clazz, final String name, final Object objs) {
        storeProperty(clazz.getName(), name, objs);
    }

    public static void storeProperty(final String className, final String name, final Object objs) {
        Gson gson = new Gson();
        properties.put(className + "#" + name, gson.toJson(objs));
    }

    public static void storeProperty(final String className, final String name, final Object[] objs) {
        Gson gson = new Gson();
        properties.put(className + "#" + name, gson.toJson(objs));
    }

    public static void storeProperty(final Class clazz, final String name, final Object[] objs) {
        storeProperty(clazz.getName(), name, objs);
    }
}
