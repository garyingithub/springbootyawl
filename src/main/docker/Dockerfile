FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD springboot-yawl-0.0.1-SNAPSHOT.jar app.jar
ADD yawldb.h2.db /tmp/yawldb.h2.db
RUN sh -c 'touch /app.jar'
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar /app.jar --yawl.master_address="${YAWL_MASTER_ADDRESS}" --yawl.engineid=${YAWL_ENGINEID} --yawl.influx_address=${YAWL_INFLUX_ADDRESS}