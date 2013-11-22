package org.freecode.irc.votebot;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Created with IntelliJ IDEA.
 * User: Deprecated
 * Date: 11/21/13
 * Time: 11:26 PM
 */
public class BootStrap {
    public static void main(String... args) {
        XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("spring/application.xml"));
        beanFactory.getBean("freeVoteBot");
    }

}
