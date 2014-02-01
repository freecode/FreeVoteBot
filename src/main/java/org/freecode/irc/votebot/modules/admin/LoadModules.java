package org.freecode.irc.votebot.modules.admin;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.freecode.irc.PrivateMsg;
import org.freecode.irc.votebot.ScriptModuleLoader;
import org.freecode.irc.votebot.api.AdminModule;
import org.freecode.irc.votebot.api.ExternalModule;

import javax.script.ScriptException;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shivam on 12/8/13.
 */
public class LoadModules extends AdminModule {

    private Repository repository;
    private Git git;
    private static final String GIT_MODULES_URL = "https://github.com/freecode/FVB-Modules.git";
    public static final File MODULES_DIR = new File(System.getProperty("user.home"), ".fvb-modules");
    private List<ExternalModule> loadedModules;

    public LoadModules() {
        super();
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            boolean exists = MODULES_DIR.exists();
            if (exists) {
                repository = builder.setGitDir(new File(MODULES_DIR, ".git"))
                        .readEnvironment().findGitDir().build();
                git = new Git(repository);
                git.pull().call();
            } else {
                git = cloneRepo();
                repository = git.getRepository();
            }
            loadedModules = new ArrayList<>();
            /*loadedModules.addAll(Arrays.asList(loadModules()));
            getFvb().addModules(loadedModules);*/
        } catch (IOException | URISyntaxException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    protected Right[] getRights() {
        return new Right[]{Right.AOP, Right.SOP, Right.FOUNDER};
    }

    public void processMessage(PrivateMsg privateMsg) {
        String command = privateMsg.getMessage().substring(getName().length() + 1).trim();
        if (git == null || repository == null) {
            privateMsg.send("Failed to load git repositories");
            if (!command.equalsIgnoreCase("clean")) {
                return;
            }
        }
        if (command.equalsIgnoreCase("pull")) {
            try {
                PullResult result = git.pull().call();
                privateMsg.send(result.isSuccessful() ? "Successfully pulled." : "Failed to pull.");
            } catch (GitAPIException e) {
                privateMsg.send(e.getMessage());
            }
        } else if (command.equalsIgnoreCase("clean")) {
            try {
                git = cloneRepo();
                repository = git.getRepository();
                privateMsg.send("Successfully cleaned");
            } catch (Exception e) {
                privateMsg.send(e.getMessage());
            }
        } else if (command.equalsIgnoreCase("reload")) {
            try {
                getFvb().removeModules(loadedModules);
                loadedModules.addAll(Arrays.asList(loadModules()));
                getFvb().addModules(loadedModules);
                privateMsg.send("Successfully reloaded");
            } catch (Exception e) {
                privateMsg.send("Error reloading: " + e.getMessage());
            }
        } else if (command.startsWith("load ")) {
            String name = command.substring(5).trim();
            if (name.matches(".*[^\\w].*")) {
                //contains a symbol that isn't a word
                privateMsg.send("Invalid name!");
            } else {
                File file = new File(MODULES_DIR, name.concat(".py"));
                if (file.exists()) {
                    try {
                        ExternalModule module = getFvb().getScriptModuleLoader()
                                .loadFromFile(file);
                        loadedModules.add(module);
                        if (getFvb().addModule(module))
                            privateMsg.send("Successfully added module");
                        else
                            privateMsg.send("Failed to add module");
                    } catch (IOException | ScriptException e) {
                        privateMsg.send("Error loading module: " + e.getMessage());
                    }
                } else {
                    privateMsg.send("File does not exist!");
                }
            }
        } else if (command.startsWith("remove ")) {
            String name = command.substring(6).trim();
            if (name.matches(".*[^\\w].*")) {
                //contains a symbol that isn't a word
                privateMsg.send("Invalid name!");
            } else {
                ExternalModule module = null;
                for (ExternalModule ext : loadedModules) {
                    if (ext.getExternalName().equals(name)) {
                        module = ext;
                        break;
                    }
                }
                if (module != null) {
                    loadedModules.remove(module);
                    if (getFvb().removeModule(module)) {
                        privateMsg.send("Successfully removed");
                    } else {
                        privateMsg.send("Failed to remove");
                    }
                }
            }
        }
    }

    private ExternalModule[] loadModules() throws IOException, ScriptException {
        ArrayList<ExternalModule> modules = new ArrayList<>();
        ScriptModuleLoader loader = getFvb().getScriptModuleLoader();
        for (File file : MODULES_DIR.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".py");
            }
        })) {

                InputStream inputStream = new FileInputStream(file);
                ExternalModule module = loader.loadFromFile(file);
                //module.setFvb(getFvb());
                modules.add(module);


        }
        return modules.toArray(new ExternalModule[modules.size()]);
    }

    private boolean rmdir(final File f) {
        if (f.exists()) {
            if (f.isDirectory()) {
                rmdir(f);
                return f.delete();
            } else {
                return f.delete();
            }
        }
        return true;
    }

    private Git cloneRepo() throws IOException, GitAPIException, URISyntaxException {
        if (MODULES_DIR.exists()) {
            rmdir(MODULES_DIR);
        }
        CloneCommand clone = Git.cloneRepository().setBare(false)
                .setDirectory(MODULES_DIR).setURI(new URL(GIT_MODULES_URL).toURI().toString());
        return clone.call();
    }

    public String getName() {
        return "modules";
    }

    @Override
    public String getParameterRegex() {
        return ".*";
    }
}
