#!/usr/bin/env bash

function verify() {
	arr=("$@")
	for i in "${arr[@]}";
		do
				if [ ! -f $i ]; then

					echo "Missing ${i}"
					exit 1
				fi
		done
}

req_files=("src/dslabs.atmostonce/AMOCommand.java" "src/dslabs.atmostonce/AMOResult.java" "src/dslabs.atmostonce/AMOApplication.java" "src/dslabs.kvstore/KVStore.java" "src/dslabs.clientserver/Messages.java" "src/dslabs.clientserver/Timers.java" "src/dslabs.clientserver/SimpleServer.java" "src/dslabs.clientserver/SimpleClient.java" "REPORT.md")
verify "${req_files[@]}"
if [[ $? -ne 0 ]]; then
    exit 1
fi

if [ $# -eq 1 ]
then
	zip "${1}.zip" src/dslabs.atmostonce/AMOCommand.java src/dslabs.atmostonce/AMOResult.java src/dslabs.atmostonce/AMOApplication.java src/dslabs.kvstore/KVStore.java src/dslabs.clientserver/Messages.java src/dslabs.clientserver/Timers.java src/dslabs.clientserver/SimpleServer.java src/dslabs.clientserver/SimpleClient.java REPORT.md
else
	echo 'Please provide your GTID, eg ./submit.sh syi73'
fi
