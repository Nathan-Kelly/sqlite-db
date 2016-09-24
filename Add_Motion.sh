#->entry       (entry_date, real_date, _dev_id*, [seq])
#->motion      (gx, gy, gz, ax, ay, az, mx, my, mz, _seq*)
#>light       (lux, _seq)

EN_DATE=$(date +"%s")
DATABASE=$1
DEV_ID=$2
RL_DATE=$3

REGEXP="^[+-]?[0-9]+\.?[0-9]*$"
GX=$4
GY=$5
GZ=$6
AX=$7
AY=$8
AZ=$9
MX=${10}
MY=${11}
MZ=${12}

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
if [ -z "$GX" ] || ! [[ "$GX" =~ ^[+-]?[0-9]+\.?[0-9]*$ ]]; then
    echo "Argument missing or invalid: GX"
    exit 1
fi
if [ -z "$GY" ] || ! [[ "$GY" =~ ^[+-]?[0-9]+\.?[0-9]*$ ]]; then
    echo "Argument missing or invalid: GY"
    exit 1
fi
if [ -z "$GZ" ] || ! [[ "$GZ" =~ ^[+-]?[0-9]+\.?[0-9]*$ ]]; then
    echo "Argument missing or invalid: GZ"
    exit 1
fi

##check a valid
if [ -z "$AX" ] || ! [[ "$AX" =~ ^[+-]?[0-9]+\.?[0-9]*$ ]]; then
    echo "Argument missing or invalid: AX"
    exit 1
fi
if [ -z "$AY" ] || ! [[ "$AY" =~ ^[+-]?[0-9]+\.?[0-9]*$ ]]; then
    echo "Argument missing or invalid: AY"
    exit 1
fi
if [ -z "$AZ" ] || ! [[ "$AZ" =~ ^[+-]?[0-9]+\.?[0-9]*$ ]]; then
    echo "Argument missing or invalid: AZ"
    exit 1
fi

##check y valid
if [ -z "$MX" ] || ! [[ "$MX" =~ ^[+-]?[0-9]+\.?[0-9]*$ ]]; then
    echo "Argument missing or invalid: MX"
    exit 1
fi

if [ -z "$MY" ] || ! [[ "$MY" =~ ^[+-]?[0-9]+\.?[0-9]*$ ]]; then
    echo "Argument missing or invalid: MY"
    exit 1
fi
if [ -z "$MZ" ] || ! [[ "$MZ" =~ ^[+-]?[0-9]+\.?[0-9]*$ ]]; then
    echo "Argument missing or invalid: MZ"
    exit 1
fi



#->entry       (entry_date, real_date, _dev_id*, [seq])
if sqlite3 $DATABASE "PRAGMA foreign_keys = ON; begin; INSERT INTO entry VALUES('$EN_DATE', '$RE_DATE', '$DEV_ID', (select max(seq) from entry) + 1); INSERT INTO motion VALUES($GX, $GY, $GZ, $AX, $AY, $AZ, $MX, $MY, $MZ, (select max(seq) from entry)); commit;" ; then
    echo "s1"
elif sqlite3 $DATABASE "PRAGMA foreign_keys = ON; begin; INSERT INTO entry VALUES('$EN_DATE', '$RE_DATE', '$DEV_ID', 0); INSERT INTO motion VALUES($GX, $GY, $GZ, $AX, $AY, $AZ, $MX, $MY, $MZ, (select max(seq) from entry)); commit;" ; then
    echo "s2"
fi

exit 0

#if sqlite3 $DATABASE "PRAGMA foreign_keys = ON; begin; INSERT INTO user VALUES('$USER', (select max(id) from user) + 1); commit;" ; then
#    echo "success"
#    exit 0
#else
#    if sqlite3 $DATABASE "PRAGMA foreign_keys = ON; begin; INSERT INTO user VALUES('$USER', 0); commit;" ; then
#	echo "success"
#	exit 0
#    else
#	echo "failure"
#	exit 1
#    fi
#fi