package org.freecode.irc.votebot.modules.admin;

import com.speed.irc.types.Privmsg;
import org.freecode.irc.votebot.PollExpiryAnnouncer;
import org.freecode.irc.votebot.api.AdminModule;
import org.freecode.irc.votebot.dao.PollDAO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CreatePollModule extends AdminModule {

    private static final long DEFAULT_LIFE_SPAN = 604800000L;
    public static final String LIFESPAN_PATTERN = "\\d{1,6}[whsdmWHSDM]?";
    public static final String CREATE_POLL_WITH_LIFESPAN_PATTERN = "!createpoll " + LIFESPAN_PATTERN + " .+";

    @Autowired
    private PollDAO pollDAO;

    @Override
    public void processMessage(final Privmsg privmsg) {
        if (privmsg.getMessage().trim().equals("!createpoll")) {
            return;
        }

        long lifeSpan = DEFAULT_LIFE_SPAN;
        final String question;
        if (privmsg.getMessage().matches(CREATE_POLL_WITH_LIFESPAN_PATTERN)) {
            try {
                final String[] parts = privmsg.getMessage().split(" ", 3);
                lifeSpan = parseExpiry(parts[1]);
                question = parts[2];
            } catch (IllegalArgumentException e) {
                privmsg.getConversable().sendMessage(e.getMessage());
                throw e;
            }
        } else {
            final String[] parts = privmsg.getMessage().split(" ", 2);
            question = parts[1];
        }

        if (question.isEmpty() || question.length() < 5) {
            privmsg.getConversable().sendMessage("Question is too short.");
            return;
        }

        try {
            final long expiration = System.currentTimeMillis() + lifeSpan;
            int id = pollDAO.addNewPoll(question.trim(), expiration, privmsg.getSender());
            privmsg.getConversable().sendMessage("Created poll, type !vote " + id + " yes/no/abstain to vote.");
            PollExpiryAnnouncer exp = new PollExpiryAnnouncer(expiration, id, getFvb());
            ScheduledFuture<?> future = getFvb().pollExecutor.scheduleAtFixedRate(exp, 5000L, 500L, TimeUnit.MILLISECONDS);
            exp.setFuture(future);
            getFvb().pollFutures.put(id, future);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long parseExpiry(String lifespan) {
        if (lifespan.matches(LIFESPAN_PATTERN)) {
            long multiplier = 1000;

            if (Character.isLetter(lifespan.charAt(lifespan.length() - 1))) {
                char c = Character.toLowerCase(lifespan.charAt(lifespan.length() - 1));
                switch (c) {
                    case 'w':
                        multiplier *= 604800;
                        break;
                    case 'h':
                        multiplier *= 3600;
                        break;
                    case 'm':
                        multiplier *= 60;
                        break;
                    case 'd':
                        multiplier *= 86400;
                        break;
                }
                lifespan = lifespan.substring(0, lifespan.length() - 1);
            }
            return Long.parseLong(lifespan) * multiplier;
        } else {
            throw new IllegalArgumentException("CreatePollModule - Given lifespan is incorrectly formatted.");
        }
    }

    @Override
    public String getName() {
        return "createpoll";
    }

    @Override
    public String getParameterRegex() {
        return ".+";
    }

    public void setPollDAO(PollDAO pollDAO) {
        this.pollDAO = pollDAO;
    }

    protected Right[] getRights() {
        return new Right[]{Right.AOP, Right.SOP, Right.FOUNDER, Right.HOP};
    }
}
