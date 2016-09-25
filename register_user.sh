#!/bin/bash

#->user        (name, id*)

DATE=$(date +"%s")
DATABASE=$1
USER=$2

#arguments: database, name
if [ -z "$2" ] ; then
    echo "missing argument: name"
    exit 1
fi

if [ -z "$1" ] ; then
    echo "missing argument: db"
    exit 1
fi

if sqlite3 $DATABASE "PRAGMA foreign_keys = ON; begin; INSERT INTO user VALUES('$USER', (select max(id) from user) + 1); commit;" ; then
    echo "success"
    sqlite3 $DATABASE "select max(id) from user"
    exit 0
else
    if sqlite3 $DATABASE "PRAGMA foreign_keys = ON; begin; INSERT INTO user VALUES('$USER', 0); commit;" ; then
	echo "success"
	sqlite3 $DATABASE "select max(id) from user"
	exit 0
    else
	echo "failure"
	exit 1
    fi
fi
