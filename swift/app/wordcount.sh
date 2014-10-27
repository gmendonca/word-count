#!/bin/bash

log=$2
log(){
printf "\nCalled as: $0: $cmdargs\n\n"
printf "Start time: "; /bin/date
printf "Running as user: "; /usr/bin/id
printf "Running on node: "; /bin/hostname
printf "Node IP address: "; /bin/hostname -I
}

log 1>&2

#cat $1 | sed 's/\ \+/\n/g' | sed 's/^[\.,-\/#!$%\^&\*;:{}=_`~()]*//g' | rev | sed 's/^[\.,-\/#!$%\^&\*;:{}=_`~()]*//g'| rev | sort | uniq -c | sed '1d' | sed 's/\ \+[^a-zA-Z0-9]//g' | sort -k1nr -k2 | awk '{ print $2 "\t " $1}'
cat $1 | sed 's/[^a-zA-Z0-9]*\ \+[^a-zA-Z0-9]*/\n/g' | sort | uniq -c | sed '1d' | sort -k1nr -k2 | awk '{ print $2 "\t " $1}'