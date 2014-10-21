#!/bin/bash

numLines="$(wc -l $1 | awk '{print $1}')"

#echo $numLines

if ((numLines>10)); then
	divLines=$((numLines/10))
fi

#echo $divLines

split -d -l$((divLines+1)) $1 out