package org.freecode.irc.votebot;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/21/13
 * Time: 11:26 PM
 */
public class BootStrap {
    public static void main(String... args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/application.xml");
        context.getBean("freeVoteBot");
    }

}
