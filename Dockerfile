FROM oracle/graalvm-ce:19.3.0
RUN gu install native-image
RUN yum install zip -y