from org.freecode.irc.votebot.api import ExternalModule


class TestMod(ExternalModule):
    def getName(self):
        return "pytest"

    def processMessage(self, privmsg):
        privmsg.send("Python test, with nicer code loading!")
        polls = self.getFvb().getPollDAO().getOpenPolls()
        s = 'polls: '
        for i in range(0, len(polls)):
            s += polls[i].getName()
            s += ' '
        privmsg.send(s)



