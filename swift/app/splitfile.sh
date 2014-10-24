#!/bin/bash

numLines="$(wc -l $1 | awk '{print $1}')"

#echo $numLines

if ((numLines>$3)); then
	divLines=$((numLines/$3))
fi

#echo $divLines

split -d -l$((divLines)) $1 $2