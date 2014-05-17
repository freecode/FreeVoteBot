package org.freecode.irc.votebot.modules.admin;

import org.freecode.irc.Privmsg;
import org.freecode.irc.votebot.api.AdminModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/22/13
 * Time: 10:52 PM
 */

public class RebuildModule extends AdminModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(RebuildModule.class);
    private String idAbbrev;
    private String idDescribe;

    public static final String LAST_ID = "pre-rebuild.commit.id.abbrev";

    public void init() {
        String last = readString(LAST_ID);
        if (idAbbrev.equalsIgnoreCase(last)) {
            return;
        }

        int commits = countCommitsSince(last);
        getFvb().sendMsg("Running " + idDescribe + ", " + commits + " new commits since last run (" + last + ")");

        store(LAST_ID, idAbbrev);
    }

    @Override
    public void processMessage(Privmsg privmsg) {
        try (BufferedWriter writer = privmsg.getIrcConnection().getWriter()) {
            writer.write("QUIT :Rebuilding!\r\n");
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("Failed to send rebuilding message.", e);
        }

        try (BufferedReader reader = executeRebuild()) {
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read shell output.", e);
        }
    }

    private static int countCommitsSince(String idAbbrev) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{
                    "/bin/sh", "-c", "git rev-list " + idAbbrev + "..HEAD | wc -l"
            });
            String line = new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
            return Integer.parseInt(line);
        } catch (IOException | NumberFormatException e) {
            LOGGER.error("Failed to count commits.", e);
        }

        return -1;
    }

    private static BufferedReader executeRebuild() throws IOException {
        Process p = Runtime.getRuntime().exec("./run.sh &");
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    @Override
    public String getName() {
        return "rebuild";
    }

    protected Right[] getRights() {
        return new Right[]{Right.FOUNDER};
    }

    public void setIdAbbrev(String idAbbrev) {
        this.idAbbrev = idAbbrev;
    }

    public void setIdDescribe(String idDescribe) {
        this.idDescribe = idDescribe;
    }

}
