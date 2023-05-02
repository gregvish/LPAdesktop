#!/bin/bash
set -e

docker build -t lpa-desktop .
docker run --rm lpa-desktop:latest \
    cat /usr/src/LPAdesktop/target/LPA-1.0.0.0-jar-with-dependencies.jar \
        > LPA-1.0.0.0-jar-with-dependencies.jar

