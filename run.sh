#!/bin/bash
if [ -f freevotepid ]
 then
    line=`head -n 1 freevotepid`
    kill line
fi
git pull
ant all
nohup java -Xmx64M -jar out/artifacts/FreeVoteBot_jar/FreeVoteBot.jar -n FreeVoteBot -u FreeVoteBot -r FreeVoteBot -h irc.rizon.net -p 6667 &
pid=$!
echo $pid > freevotepid
