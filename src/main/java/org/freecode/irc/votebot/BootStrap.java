package org.freecode.irc.votebot;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/21/13
 * Time: 11:26 PM
 */
@Configuration
@ComponentScan(basePackages = "org.freecode.irc.votebot")
@ImportResource({"classpath:spring/application.xml", "classpath:spring/datasource.xml",})
public class BootStrap {

    public static void main(String[] args) {
        ApplicationContext app = new AnnotationConfigApplicationContext(BootStrap.class);

        FreeVoteBot fvb = app.getBean(FreeVoteBot.class);
    }
}
