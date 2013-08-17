#!/bin/bash
if [-a freevotepid] then
    line=$(read -r line < freevotepid)
    kill line
fi
git pull
ant all
nohup java -Xmx64M -jar out/artifacts/FreeVoteBot_jar/FreeVoteBot.jar -n FreeVoteBot -u FreeVoteBot -r FreeVoteBot -h irc.rizon.net -p 6667 &
pid=$!
echo pid > freevotepid
