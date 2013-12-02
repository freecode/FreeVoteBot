from org.freecode.irc.votebot.api import ExternalModule

class TestMod(ExternalModule):

    def getName(self):
        return "pytest"

    def processMessage(self, privmsg):
        privmsg.send("Python test!")

module = TestMod()