package org.freecode.irc.votebot;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by martin on 14/05/14.
 */

@Component
public class PropertyStore {

    public static final File STORE_DIR = new File(System.getProperty("user.home"), ".fvb-store");

    Gson gson = new Gson();
    Properties keyValues = new Properties();

    public void store(String key, Object value) {
        keyValues.setProperty(key, gson.toJson(value));

        //or preferably on shutdown
        save();
    }

    public String getRawProperty(String key) {
        return keyValues.getProperty(key);
    }

    public <T> T getProperty(String key, Class<T> classOfT) {
        return gson.fromJson(getRawProperty(key), classOfT);
    }

    public void clearProperty(String key) {
        keyValues.remove(key);
        save();
    }

    public void load() {
        try {
            keyValues.load(new FileReader(STORE_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            keyValues.store(new FileWriter(STORE_DIR), "FVB module keyvalues");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
