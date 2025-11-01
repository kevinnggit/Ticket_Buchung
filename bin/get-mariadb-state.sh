#!/usr/bin/env bash
mariadb -e 'select id, user, host, db, command, time_ms, state, progress from information_schema.processlist' | nl
