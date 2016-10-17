#!/bin/bash


# Change this to your netid
netid=kxm153630


# Root directory of your project
PROJDIR=/home/012/k/kx/kxm153630/com.aos.lab1

CONFIG=$PROJDIR/conf/config.txt

#
# Directory your java classes are in
#
BINDIR=$PROJDIR/bin

#
# Your main project class
#
PROG=com.aos.lab1.Process


cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | grep "\s*[0-9]\+\s*\w\+.*" |
(
    while read line 
    do
	n=$( echo $line | awk '{ print $1 }' )
	host=$( echo $line | awk '{ print $2 }' )
	nodeId=$( echo $line | awk '{ print $1 }' )
    ssh -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR:$PROJDIR/lib/* -Dconfig=$CONFIG -Dlog4j.configurationFile=/home/012/k/kx/kxm153630/com.aos.lab1/conf/log4j.xml -DnodeId=$nodeId $PROG $n &
    done
)



