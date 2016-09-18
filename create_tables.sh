#!/bin/bash

#sqlite3 test2.db "PRAGMA foreign_keys = ON;"
#->user        (name, [id])
#->dev         (id*, [dev_id])
#->entry       (entry_date, real_date, _dev_id*, [seq])
#->motion      (gx, gy, gz, ax, ay, az, mx, my, mz, _seq*)

sqlite3 $1 "PRAGMA foreign_keys = ON; CREATE TABLE user(name TEXT NOT NULL, id INT PRIMARY KEY NOT NULL);"
sqlite3 $1 "PRAGMA foreign_keys = ON; CREATE TABLE dev(_id INT NOT NULL, dev_id TEXT PRIMARY KEY NOT NULL, foreign key(_id) references user(id));"
sqlite3 $1 "PRAGMA foreign_keys = ON; CREATE TABLE entry (entry_date DATE NOT NULL, real_date DATE NOT NULL, _dev_id TEXT NOT NULL, seq INT PRIMARY KEY NOT NULL, foreign key(_dev_id) references dev(dev_id));"
sqlite3 $1 "PRAGMA foreign_keys = ON; create table motion(gx REAL NOT NULL, gy REAL NOT NULL, gz REAL NOT NULL, ax REAL NOT NULL, ay REAL NOT NULL, az REAL NOT NULL, mx REAL NOT NULL, my REAL NOT NULL, int REAL NOT NULL, _seq INT NOT NULL, foreign key(_seq) references entry(seq));"
