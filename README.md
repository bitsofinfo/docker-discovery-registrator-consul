# docker-discovery-registrator-consul

Service discovery library for JVM based applications running in Docker containers that use the Registrator service registry bridge with Consul as a backend. 

The purpose of this library is for "self-discovery" from within your JVM based Docker application where you need to discover what
your accessible docker-host bound IP and mapped port(s) are. This is critical if your container has to do further peer discovery
for other services it provides or clustering groups it must form.

* [Status](#status)
* [Releases](#releases)
* [Requirements](#requirements)
* [Maven/Gradle install](#mavengradle)
* [Features](#features)
* [Usage](#usage)
* [Build from source](#building)
* [Unit tests](#tests)
* [Related Info](#related)
* [Todo](#todo)
* [Notes](#notes)
* [Docker info](#docker)


## <a id="status"></a>Status

Beta code. Master branch available only.

## <a id="releases"></a>Releases

No official releases yet.

* MASTER - in progress, this README refers to what is in the master branch. Switch to relevant RELEASE tag above to see that versions README

## <a id="requirements"></a>Requirements

* Java 6+
* Your application is running in a Docker container, using this library for discovery
* Your Docker host has a [Registrator](https://github.com/gliderlabs/registrator) container running prior to launch of your app
* The Registrator container is configured to use [Consul](https://consul.io/) as its registry backend

## <a id="mavengradle"></a>Maven/Gradle

To use this discovery strategy in your Maven or Gradle project use the dependency samples below. (coming soon)

### Gradle:

coming soon

### Maven:

coming soon

## <a id="features"></a>Features

* Permits a JVM based container application to self-discover all of its mapped ports and accessible ip address, as well of that as all of its peer "services" that share the same service name, ports and/or service tags, as set by Registrator in Consul.


## <a id="usage"></a>Usage Sample

* Review the code in [SampleContainerApp.java](src/main/java/org/bitsofinfo/docker/discovery/registrator/consul/sample/SampleContainerApp.java)

* Have Consul running and available somewhere on your network, start it such as:
    ```
    consul agent -server -bootstrap-expect 1 -data-dir /tmp/consul -config-dir /path/to/consul.d/ -ui-dir /path/to/consul-web-ui -bind=0.0.0.0 -client=0.0.0.0
    ```

* On your Docker host ensure Registrator is running such as:
    ```
    docker run -d --name=registrator --net=host --volume=/var/run/docker.sock:/tmp/docker.sock  gliderlabs/registrator:latest consul://[YOUR_CONSUL_IP]:8500
    ```

* In the root of the project:

    ```
    cd sample/
    ./build-image.sh
    ```

    ```
    docker images

    REPOSITORY                                    TAG                     IMAGE ID            CREATED             VIRTUAL SIZE
    docker-discovery-registrator-consul-sample    latest                  750dc9aa5052        18 minutes ago      651.5 MB
    ```

* Now run the sample image you built 3x, (you can do more if you want)

	```
	docker run -e "SERVICE_TAGS=dev,myUniqueId001" --rm=true -P docker-discovery-registrator-consul-sample:latest java -DMY_SERVICE_NAME=docker-discovery-registrator-consul-sample -DMY_UNIQUE_TAG=myUniqueId001 -DCONSUL_IP=[YOUR_CONSUL_IP] -DCONSUL_PORT=8500 -DSERVICE_NAME_STRATEGY=org.bitsofinfo.docker.discovery.registrator.consul.MultiServiceNameSinglePortStrategy -jar /sample/sample.jar
	
	docker run -e "SERVICE_TAGS=dev,myUniqueId002" --rm=true -P docker-discovery-registrator-consul-sample:latest java -DMY_SERVICE_NAME=docker-discovery-registrator-consul-sample -DMY_UNIQUE_TAG=myUniqueId002 -DCONSUL_IP=[YOUR_CONSUL_IP] -DCONSUL_PORT=8500 -DSERVICE_NAME_STRATEGY=org.bitsofinfo.docker.discovery.registrator.consul.MultiServiceNameSinglePortStrategy -jar /sample/sample.jar
	
	docker run -e "SERVICE_TAGS=dev,myUniqueId003" --rm=true -P docker-discovery-registrator-consul-sample:latest java -DMY_SERVICE_NAME=docker-discovery-registrator-consul-sample -DMY_UNIQUE_TAG=myUniqueId003 -DCONSUL_IP=[YOUR_CONSUL_IP] -DCONSUL_PORT=8500 -DSERVICE_NAME_STRATEGY=org.bitsofinfo.docker.discovery.registrator.consul.MultiServiceNameSinglePortStrategy -jar /sample/sample.jar
	```

* Every 10 seconds, each instance of the sample container app, will report the discovery information about itself and its peers:


	From container one's perspective:
	```
	########## myUniqueId001 REPORTING: ##########
	MY SERVICES:
	/192.168.99.100:32846 -> container:8080 tags: [dev, myUniqueId001]
	/192.168.99.100:32845 -> container:8443 tags: [dev, myUniqueId001]
	
	
	MY PEER SERVICES:
	/192.168.99.100:32842 -> container:8080 tags: [dev, myUniqueId003]
	/192.168.99.100:32844 -> container:8080 tags: [dev, myUniqueId002]
	/192.168.99.100:32841 -> container:8443 tags: [dev, myUniqueId003]
	/192.168.99.100:32843 -> container:8443 tags: [dev, myUniqueId002]
	########## END myUniqueId001 ############
	```
	
	From container two's perspective:
	```
	########## myUniqueId002 REPORTING: ##########
	MY SERVICES:
	/192.168.99.100:32844 -> container:8080 tags: [dev, myUniqueId002]
	/192.168.99.100:32843 -> container:8443 tags: [dev, myUniqueId002]
	
	
	MY PEER SERVICES:
	/192.168.99.100:32842 -> container:8080 tags: [dev, myUniqueId003]
	/192.168.99.100:32846 -> container:8080 tags: [dev, myUniqueId001]
	/192.168.99.100:32841 -> container:8443 tags: [dev, myUniqueId003]
	/192.168.99.100:32845 -> container:8443 tags: [dev, myUniqueId001]
	########## END myUniqueId002 ############
	```
	
	From container three's perspective:
	```
	########## myUniqueId003 REPORTING: ##########
	MY SERVICES:
	/192.168.99.100:32842 -> container:8080 tags: [dev, myUniqueId003]
	/192.168.99.100:32841 -> container:8443 tags: [dev, myUniqueId003]
	
	
	MY PEER SERVICES:
	/192.168.99.100:32844 -> container:8080 tags: [dev, myUniqueId002]
	/192.168.99.100:32846 -> container:8080 tags: [dev, myUniqueId001]
	/192.168.99.100:32843 -> container:8443 tags: [dev, myUniqueId002]
	/192.168.99.100:32845 -> container:8443 tags: [dev, myUniqueId001]
	########## END myUniqueId003 ############
	```

## <a id="building"></a>Building from source

* From the root of this project, build a Jar : `./gradlew assemble`

* Include the built jar artifact located at `build/libs/docker-discovery-registrator-consul-[VERSION].jar` in your JVM based project

* If not already present in your hazelcast application's Maven (pom.xml) or Gradle (build.gradle) dependencies section; ensure that these dependencies are present (versions may vary as appropriate):
	
	```
	compile group: 'com.orbitz.consul', name: 'consul-client', version:'0.10.0'
	compile 'javax.ws.rs:javax.ws.rs-api:2.0.1'
	compile 'org.glassfish.jersey.core:jersey-client:2.22.2'
	compile 'org.slf4j:slf4j-api:1.7.19'
	```


## <a id="tests"></a>Unit-tests

Coming soon

## <a id="related"></a>Related info

* https://www.consul.io
* https://github.com/gliderlabs/registrator

## <a id="todo"></a>Todo

* Coming soon

## <a id="notes"></a> Notes

* Coming soon
