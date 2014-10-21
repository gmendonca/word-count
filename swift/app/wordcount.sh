#!/bin/bash

cat $1 | sed 's/\ \+/\n/g' | sed 's/[^a-zA-Z0-9]*//' | rev | sed 's/[^a-zA-Z0-9]*//'| rev | sort | uniq -c | sed '1d' | sed 's/\ \+[^a-zA-Z0-9]//g' | sort -k1nr -k2 | awk '{ print $2 "\t " $1}'