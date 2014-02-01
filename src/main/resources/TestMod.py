from org.freecode.irc.votebot.api import ExternalModule


class TestMod(ExternalModule):
    def getName(self):
        return 'pytest'

    def processMessage(self, privateMsg):
        privateMsg.send('Python test, with nicer code loading!')
        polls = self.getFvb().getPollDAO().getOpenPolls()
        s = 'polls: ' + len(polls)
        privateMsg.send(s)



