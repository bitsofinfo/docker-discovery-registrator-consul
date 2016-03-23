FROM java:8
MAINTAINER bitsofinfo.g@gmail.com

######################
# IMAGE DETAILS
######################

RUN mkdir -p /sample

ADD docker-discovery-registrator-consul-1.0-RC1-fat.jar /sample/sample.jar

EXPOSE 8080 8443
 
ADD docker-entrypoint.sh /entrypoint.sh 
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

CMD ["java"]
 
