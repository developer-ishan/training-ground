#!/bin/zsh

db_set(){
    echo "$1,$2" >> database
}

db_get(){
    grep "^$1," database | sed -e "s/$1,/key -> /" | tail -n 1
}