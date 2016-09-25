#!/bin/bash

#->user        (name, [id])
#->dev         (id*, [dev_id])

DATE=$(date +"%s")
DATABASE=$1
USER=$2 #should be integer
DEV=$3 #should not be empty
USAGE="usage: database, user(int-fk), dev_id(str-id)"
#arguments: database, name
if [ -z "$3" ] ; then
    echo "missing argument: device_id"
    echo $USAGE
    exit 1
fi

if ! [[ $USER =~ ^[0-9]+$ ]] ; then
    echo "user expects an integer type"
    echo $USAGE
    exit 1
fi

if [ -z "$1" ] ; then
    echo "missing argument: db"
    echo $USAGE
    exit 1
fi
if sqlite3 $DATABASE "PRAGMA foreign_keys = ON; begin; INSERT INTO dev VALUES($USER, \"$3\"); commit;" ; then
    echo "success"
    exit 0
fi

echo $USAGE
exit 1
