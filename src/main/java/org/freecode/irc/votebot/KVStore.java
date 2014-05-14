package org.freecode.irc.votebot;

import com.google.gson.Gson;

import java.io.*;
import java.util.Properties;

/**
 * Created by martin on 14/05/14.
 */
public class KVStore {

    public static final File STORE_DIR = new File(System.getProperty("user.home"), ".fvb-store");

    Gson gson = new Gson();
    Properties keyValues = new Properties();

    public void store(String key, Object value) {
        keyValues.setProperty(key, gson.toJson(value));

        //or preferably on shutdown
        save();
    }

    public String readJson(String key) {
        return keyValues.getProperty(key);
    }
    public <T> T read(String key, Class<T> classOfT) {
        return gson.fromJson(readJson(key), classOfT);
    }

    public void load()   {
        try {
            keyValues.load(new FileReader(STORE_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save()  {
        try {
            keyValues.store(new FileWriter(STORE_DIR), "FVB module keyvalues");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
