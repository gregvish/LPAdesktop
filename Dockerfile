FROM openjdk:8

RUN apt update
RUN apt install maven genisoimage -y

COPY . /usr/src/LPAdesktop

WORKDIR /usr/src/LPAdesktop/LPAd_SM-DPPlus_Connector
RUN mvn install
WORKDIR /usr/src/LPAdesktop
RUN mvn install

