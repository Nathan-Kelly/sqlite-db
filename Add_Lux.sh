#->entry       (entry_date, real_date, _dev_id*, [seq])
#->motion      (gx, gy, gz, ax, ay, az, mx, my, mz, _seq*)
#>light       (lux, _seq)

EN_DATE=$(date +"%s")
DATABASE=$1
DEV_ID=$2
RL_DATE=$3
LUX=$4
REGEXP="^[+-]?[0-9]+\.?[0-9]*$"

#arguments: database, name
if [ -z "$2" ] ; then
    echo "missing argument: dev_id"
    exit 1
fi

if [ -z "$1" ] ; then
    echo "missing argument: db"
    exit 1
fi

#arguments: database, name
if [ -z "$3" ] ; then
    echo "missing argument: date"
    exit 1
fi

##check g valid
if [ -z "$LUX" ] || ! [[ "$LUX" =~ ^[+-]?[0-9]+\.?[0-9]*$ ]]; then
    echo "Argument missing or invalid: LUX"
    exit 1
fi

##check if valid

#->entry       (entry_date, real_date, _dev_id*, [seq])
if sqlite3 $DATABASE "PRAGMA foreign_keys = ON; begin; INSERT INTO entry VALUES('$EN_DATE', '$RL_DATE', '$DEV_ID', (select max(seq) from entry) + 1); INSERT INTO lux VALUES($LUX,(select max(seq) from entry)); commit;" ; then
    echo "s1"
    exit 0
elif sqlite3 $DATABASE "PRAGMA foreign_keys = ON; begin; INSERT INTO entry VALUES('$EN_DATE', '$RL_DATE', '$DEV_ID', 0); INSERT INTO lux VALUES($LUX, (select max(seq) from entry)); commit;" ; then
    echo "s2"
    exit 0
fi
echo "could not enter in database"
exit 0
