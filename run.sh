#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Load .env if present
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

# Ensure local project MySQL instance on port 3307 is available if 3306 requires unconfigured credentials
if [ -x "/opt/homebrew/bin/mysqld" ] && ! nc -z 127.0.0.1 3307 >/dev/null 2>&1; then
    echo "Starting project MySQL instance (swap_ed_db) on port 3307..."
    if [ ! -d "/tmp/swaped-mysql/mysql" ]; then
        rm -rf /tmp/swaped-mysql
        mkdir -p /tmp/swaped-mysql
        /opt/homebrew/bin/mysqld --initialize-insecure --datadir=/tmp/swaped-mysql --basedir=/opt/homebrew/opt/mysql
    fi
    /opt/homebrew/bin/mysqld --datadir=/tmp/swaped-mysql --basedir=/opt/homebrew/opt/mysql --port=3307 --socket=/tmp/swaped-mysql.sock --mysqlx=0 --pid-file=/tmp/swaped-mysql.pid --log-error=/tmp/swaped-mysql.err --daemonize
    /opt/homebrew/bin/mysql -h 127.0.0.1 -P 3307 -u root -e "CREATE DATABASE IF NOT EXISTS swap_ed_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
fi

chmod +x "$SCRIPT_DIR/mvnw"
exec "$SCRIPT_DIR/mvnw" spring-boot:run
