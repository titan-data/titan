FROM oracle/graalvm-ce:19.0.0
RUN gu install native-image
RUN yum install zip -y