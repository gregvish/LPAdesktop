#!/bin/bash
set -e

export APDU_PORT="${1:-11321}" 

java -javaagent:./extract-tls-secrets-4.0.0.jar=/tmp/secrets.log \
     -jar LPA-1.0.0.0-jar-with-dependencies.jar

