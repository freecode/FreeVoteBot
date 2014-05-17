#!/usr/bin/env bash
if [ -f freevotepid ]
 then
    line=`head -n 1 freevotepid`
    kill $line
fi
git pull >> git.log
mvn clean package
nohup java -cp `cat target/classpath.cp`:target/FreeVoteBot-1.0.0.jar org.freecode.irc.votebot.BootStrap > /dev/null 2>&1 &
#nohup java -cp `cat target/classpath.cp`:target/FreeVoteBot-1.0.0.jar org.freecode.irc.votebot.BootStrap &
#java -cp `cat target/classpath.cp`:target/FreeVoteBot-1.0.0.jar org.freecode.irc.votebot.BootStrap 
pid=$!
echo $pid > freevotepid
