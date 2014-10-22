#!/bin/bash

numLines="$(wc -l $1 | awk '{print $1}')"

#echo $numLines

if ((numLines>10)); then
	divLines=$((numLines/10))
fi

#echo $divLines

#split -d -l$((divLines+1)) $1 $2


for i in {0..9}
do
	if((i==0));then
		range[i]=$((divLines+1))
	elif((i==9));then
		range[i]=$((numLines))
	else
		range[i]=$((range[i-1]+divLines+1))
	fi
done

sed ''range[$(($2-1))]','range[$2]'!d' $1