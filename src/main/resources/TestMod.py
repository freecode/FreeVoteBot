from org.freecode.irc.votebot.api import ExternalModule

class TestMod(ExternalModule):

    def getName(self):
        return "pytest"

    def processMessage(self, privmsg):
        privmsg.send("Python test, with nicer code loading!")
        polls = getFvb().getOpenPolls()
        if polls is None:
            privmsg.send("No open polls!")
        else:
            privmsg.send(len(polls))



#module = TestMod()