#!/bin/bash


# Change this to your netid
netid=axr140930

#
# Root directory of your project
PROJDIR=/home/010/a/ax/axr140930/AOS_Project2/com.aos.lab2

#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
CONFIG=$PROJDIR/conf/config.txt

#
# Directory your java classes are in
#
BINDIR=$PROJDIR/bin

#
# Your main project class
#
PROG=com.aos.lab2.Process

n=1

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    while read line 
    do
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        ssh $netid@$host killall -u $netid &
        sleep 1

        n=$(( n + 1 ))
    done
   
)


echo "Cleanup complete"
