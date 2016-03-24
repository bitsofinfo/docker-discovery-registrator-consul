# docker-discovery-registrator-consul

Service discovery library for JVM based applications running in Docker containers that use the [Registrator](https://github.com/gliderlabs/registrator) service registry bridge with [Consul](https://consul.io/) as a backend. 

The purpose of this library is for "self-discovery" from within your JVM based Docker application where you need to discover what
your accessible docker-host bound IP and mapped port(s) are, as well as your peers within the same service. This is critical if your container has to do further peer discovery
for other services it provides or clustering groups it must form.

* [Status](#status)
* [Releases](#releases)
* [Requirements](#requirements)
* [Maven/Gradle install](#mavengradle)
* [Features](#features)
* [Usage Overview](#usageoverview)
* [Running example](#runningexample)
* [Build from source](#building)
* [Unit tests](#tests)
* [Related Info](#related)
* [Todo](#todo)
* [Notes](#notes)
* [Docker info](#docker)

![Diagram of docker discovery consul registrator](/docs/diag1.png "Diagram1")

## <a id="status"></a>Status

Beta code. Master branch available only.

## <a id="releases"></a>Releases

* MASTER - in progress, this README refers to what is in the master branch. Switch to relevant RELEASE tag above to see that versions README

* [1.0-RC1](https://github.com/bitsofinfo/docker-discovery-registrator-consul/releases/tag/1.0-RC1)

## <a id="requirements"></a>Requirements

* Java 6+
* Your application is running in a Docker container, using this library for discovery
* Your Docker host has a [Registrator](https://github.com/gliderlabs/registrator) container running prior to launch of your app
* The Registrator container is configured to use [Consul](https://consul.io/) as its registry backend

## <a id="mavengradle"></a>Maven/Gradle

To use this discovery strategy in your Maven or Gradle project use the dependency samples below. (coming soon)

### Gradle:

```
repositories {
    jcenter()
}

dependencies {
    compile 'org.bitsofinfo:docker-discovery-registrator-consul:1.0-RC1'

    // include your preferred javax.ws.rs-api implementation
    // (for the OrbitzWorldwide/consul-client dependency)
    // for example below:
    compile 'javax.ws.rs:javax.ws.rs-api:2.0.1'
    compile 'org.glassfish.jersey.core:jersey-client:2.22.2'
    compile 'org.slf4j:slf4j-api:1.7.19'
}
```

### Maven:

```
<dependencies>
    <dependency>
        <groupId>org.bitsofinfo</groupId>
        <artifactId>docker-discovery-registrator-consul</artifactId>
        <version>1.0-RC1</version>
    </dependency>

    <!-- include your preferred javax.ws.rs-api
         (for the https://github.com/OrbitzWorldwide/consul-client dependency)
         implementation - see gradle example above
    -->
</dependencies>

<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>central</id>
        <name>bintray</name>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>
```

## <a id="features"></a>Features

* Permits a JVM based container application to self-discover all of its mapped ports and accessible ip address, as well of that as all of its peer "services" that share the same service name, ports and/or service tags, as set by Registrator in Consul.


## <a id="usageoverview"></a>Usage overview

Its **highly recommended** that you walking the example below in the section below. Overall the concept and API is quite simple.

1. You launch your container that utilizes this library, passing both Registrator (via `-e` args) and your container the required arguments (or via other configuration means) about how to connect to Consul, its unique identifier, serviceName, and any "tags" it might need to know about related to discovery.

2. In your app's code, you create a new [ConsulDiscovery](src/main/java/org/bitsofinfo/docker/discovery/registrator/consul/ConsulDiscovery.java)
instance, giving it the required constructor args or properties via the builder syntax for how to connect to Consul, its unique ID, serviceName,
tags etc.

3. Once constructed you can call the various methods on [ConsulDiscovery](src/main/java/org/bitsofinfo/docker/discovery/registrator/consul/ConsulDiscovery.java) such as `discoverPeers()`, `discoverMe()`, `discoverPeers(portFilter)`, `discoverMe(portFilter)`, which returns a 
collection of [ServiceInfo](src/main/java/org/bitsofinfo/docker/discovery/registrator/consul/ServiceInfo.java) instances, each representing
a specific `ip:port` binding for the participating node that shares the `service-name` as Registrator placed in Consul.

4. There are two different strategies that can be used when setting up your [ConsulDiscovery](src/main/java/org/bitsofinfo/docker/discovery/registrator/consul/ConsulDiscovery.java) instance. They are as follows, (see javadoc in source for more details):

  * [MultiServiceNameSinglePortStrategy](src/main/java/org/bitsofinfo/docker/discovery/registrator/consul/MultiServiceNameSinglePortStrategy.java) - See source code javadoc for details. Used when you specify a Registrator consumed environment variable `-e SERVICE_[port]_NAME` or completely omit it entirely (i.e. you don't pass any `-e SERVICE_[port]_NAME=xxx`). The result is a unique `<serviceName>-<port>` service registered in the Consul catalog by Registrator for each unique port exposed by your container. All nodes exposing a port are listed under the same `<serviceName>-<port>` in the Consul service catalog. See [Registrator doc](http://gliderlabs.com/registrator/latest/user/services/#service-name) 
  
  * [OneServiceNameMultiPortStrategy](src/main/java/org/bitsofinfo/docker/discovery/registrator/consul/OneServiceNameMultiPortStrategy.java) - See source code javadoc for details. Used when you specify a `-e SERVICE_NAME=xxx` shared service name syntax (note, there is NO port info in service name). The result is you end up with one service name in Consul yielding many unique `node:port` listings, one for every port exposed by a node sharing that service name. (i.e. if you have 2 ports per node, and 2 containers, the service listing yields 4 listings) See [Registrator doc](http://gliderlabs.com/registrator/latest/user/services/#service-name)
  
#### Sample code:

```
ConsulDiscovery c = new ConsulDiscovery()
                        .setConsulIp("192.168.0.200")
                        .setConsulPort(8500)
                        
                        // "service name" base in Consul that Registrator
                        // will list this container-app under. Either just 
                        // "serviceName" or "serviceName-port" depending on
                        // what -e variable approach you took when launching
                        // the container see: http://gliderlabs.com/registrator/latest/user/services/#service-name
                        .setServiceName("my-app-name") 
                        
                         // both Registrator (via -e SERVICE_TAGS=[..,thisUniqueId001]) AND your app need this! 
                        .setMyNodeUniqueTagId("thisUniqueId001") 
                        
                        // what ports you care about discovering info about
                        .addPortToDiscover(8080)
                        .addPortToDiscover(8443)
                        
                         // optional, additional tag to filter on
                         // again Registrator  needs this too via (via -e SERVICE_TAGS=[staging,tag2,thisUniqueId001]) 
                        .addMustHaveTag("staging")
                        
                        // the service name strategy to use, expained above, see JavaDoc in 
                        // ServiceNameStrategy implementations for specifics
                        .setServiceNameStrategyClass(MultiServiceNameSinglePortStrategy.class);
                        
// the port is optional, see Javadoc
// yields a list of all ServiceInfo's for all nodes
// other than THIS node, which have that port mapped.
Collection<ServiceInfo> allServiceNodes = c.discoverPeers(8443);

```


## <a id="runningexample"></a>Running example

* The simplist way to see how to use this library is first, review the code in [SampleContainerApp.java](src/main/java/org/bitsofinfo/docker/discovery/registrator/consul/sample/SampleContainerApp.java). The API is quite simple and fairly straight forward.

* Have Consul running and available somewhere on your network, start it such as: (adjust paths below)
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

* Now run the sample image you built 3x, (you can do more if you want). NOTE! Be sure to adjust the `-DMY_UNIQUE_TAG=`, `-DCONSUL_IP=[YOUR_CONSUL_IP]`, and ` -e "SERVICE_TAGS=dev,myUniqueId003"` properties below in each command for each instance launched to give it a unique id, and correct consul ip.

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
