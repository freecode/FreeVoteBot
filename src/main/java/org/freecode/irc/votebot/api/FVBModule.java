package org.freecode.irc.votebot.api;

import com.google.gson.Gson;
import org.freecode.irc.Transmittable;
import org.freecode.irc.votebot.FreeVoteBot;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: shivam
 * Date: 10/28/13
 * Time: 6:02 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FVBModule implements Runnable {
    private final FreeVoteBot fvb;
    private volatile boolean enabled = true;
    protected static Properties properties = new Properties();

    public abstract boolean canRun(final Transmittable trns);

    public abstract void process(final Transmittable trns);

    public abstract String getName();

    public FVBModule(final FreeVoteBot fvb) {
        this.fvb = fvb;
    }


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

    public FreeVoteBot getFvb() {
        return fvb;
    }
}
