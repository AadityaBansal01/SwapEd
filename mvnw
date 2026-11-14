#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
elif [ -x "/tmp/apache-maven-3.9.9/bin/mvn" ]; then
    exec "/tmp/apache-maven-3.9.9/bin/mvn" "$@"
elif [ -x "$SCRIPT_DIR/.mvn/apache-maven-3.9.9/bin/mvn" ]; then
    exec "$SCRIPT_DIR/.mvn/apache-maven-3.9.9/bin/mvn" "$@"
else
    echo "Downloading Apache Maven 3.9.9..."
    mkdir -p "$SCRIPT_DIR/.mvn"
    curl -sL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.tar.gz -o "$SCRIPT_DIR/.mvn/maven.tar.gz"
    tar -xzf "$SCRIPT_DIR/.mvn/maven.tar.gz" -C "$SCRIPT_DIR/.mvn"
    rm -f "$SCRIPT_DIR/.mvn/maven.tar.gz"
    exec "$SCRIPT_DIR/.mvn/apache-maven-3.9.9/bin/mvn" "$@"
fi
