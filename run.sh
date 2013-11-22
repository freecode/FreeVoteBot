#!/bin/bash
if [ -f freevotepid ]
 then
    line=`head -n 1 freevotepid`
    kill $line
fi
git pull
mvn package
nohup java -Xmx64M -jar target/FreeVoteBot-1.0.0-jar-with-dependencies.jar &
pid=$!
echo $pid > freevotepid
