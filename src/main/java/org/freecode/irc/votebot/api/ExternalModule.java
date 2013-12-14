package org.freecode.irc.votebot.api;

import org.freecode.irc.votebot.FreeVoteBot;

/**
 * Abstract class used for modules loaded from the web
 * Does not rely on dependency injection from Spring
 *
 * @author Shivam Mistry
 */
public abstract class ExternalModule extends CommandModule {

    private FreeVoteBot fvb;
    private String extName;


    /**
     * Gets the instance of {@link FreeVoteBot} that this module is linked to
     *
     * @return instance of {@link FreeVoteBot} that this module is linked to
     */
    public final FreeVoteBot getFvb() {
        return fvb;
    }

    public final void setFvb(FreeVoteBot bot) {
        fvb = bot;
    }

    public final String getExternalName() {
        return extName;
    }

    public final void setExtName(String extName) {
        this.extName = extName;
    }
}
