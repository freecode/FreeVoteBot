package org.freecode.irc.votebot;

import org.freecode.irc.votebot.api.ExternalModule;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.net.URI;

/**
 * Loads scripted modules for FreeVoteBot.
 * Modules may be scripted in JRuby or JavaScript (Rhino)
 *
 * @author Shivam Mistry
 */

public class ScriptModuleLoader {


    private final FreeVoteBot fvb;
    private final ScriptEngine rubyEngine, rhinoEngine;
    private ScriptEngineManager manager;


    /**
     * Initialises a ScriptModuleLoader
     *
     * @param fvb the instance of {@link FreeVoteBot} where the modules should be loaded to
     */
    public ScriptModuleLoader(FreeVoteBot fvb) {
        this.fvb = fvb;
        manager = new ScriptEngineManager();
        rubyEngine = manager.getEngineByExtension("rb");
        rhinoEngine = manager.getEngineByName("JavaScript");
    }

    private ExternalModule loadRubyModule(final InputStream in) throws ScriptException {
        InputStreamReader reader = new InputStreamReader(in);
        rubyEngine.eval(reader);
        Invocable inv = (Invocable) rubyEngine;
        ExternalModule externalModule = inv.getInterface(ExternalModule.class);
        externalModule.setFvb(fvb);
        return externalModule;
    }

    private ExternalModule loadJsModule(final InputStream in) throws ScriptException {
        InputStreamReader reader = new InputStreamReader(in);
        rhinoEngine.eval(reader);
        Invocable inv = (Invocable) rhinoEngine;
        ExternalModule externalModule = inv.getInterface(ExternalModule.class);
        externalModule.setFvb(fvb);
        return externalModule;
    }


    /**
     * Loads a module from a JavaScript or Ruby file that implements an ExternalModule
     *
     * @param file the file to load the {@link ExternalModule} from
     * @return the {@link ExternalModule} loaded, or <tt>null</tt>
     * @throws IOException     if the file was not found
     * @throws ScriptException if the script failed to load
     */
    public ExternalModule loadFromFile(final File file) throws IOException, ScriptException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath() + " does not exist!");
        }
        FileInputStream fileIn = new FileInputStream(file);
        if (file.getName().endsWith(".rb")) {
            return loadRubyModule(fileIn);
        } else if (file.getName().endsWith(".js")) {
            return loadJsModule(fileIn);
        } else {
            return null;
        }
    }
}
