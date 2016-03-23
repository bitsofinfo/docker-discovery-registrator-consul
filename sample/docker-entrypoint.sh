#!/bin/bash

set -e

command="$1"

if [ "$command" == "java" ];
then 
	echo "Launching Sample"; 
else 
	echo "ERROR: command must start with: java"; 
	exit 1; 
fi

exec "$@"