/**
 * Created with IntelliJ IDEA.
 * User: shivam
 * Date: 11/30/13
 * Time: 1:47 AM
 * To change this template use File | Settings | File Templates.
 */


var m = {
    processMessage: function (privmsg) {
        privmsg.send("Javascript works!");
    },

    getName: function () {
        return new java.lang.String("jstest");
    }
};

var module = new org.freecode.irc.votebot.api.ExternalModule(m);



