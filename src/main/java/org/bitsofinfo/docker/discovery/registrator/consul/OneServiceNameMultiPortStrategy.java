package org.bitsofinfo.docker.discovery.registrator.consul;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbitz.consul.CatalogClient;

/**
* Strategy for Docker Registrator generated service names 
* where the Registrator monitored Docker container was started 
* by explicity setting an explicit shared SERVCE_NAME as follows:
* 
* see: http://gliderlabs.com/registrator/latest/user/services/#service-name 
* 
* docker run -e "SERVICE_NAME=my-service-name" "SERVICE_TAGS=tag1,tag2,myUniqueContainerId" [CONTAINER_NAME|ID]
* 
* i.e. in consul it would be fetched as:
* 
* GET /v1/catalog/services
* 
	{
	  "consul":[  ],
	  "my-service-name":[
	    "tag1",
	    "tag2",
	    "uniqueContainerId1",
	    "uniqueContainerId2"
	  ]
	}
* 
* GET /v1/catalog/service/my-service-name
* 
* Which yields ALL containers with that service name, each with a unique service listed for each unique port exposed
* 
	[
	  {
	    "Node":"my.consul.node",
	    "Address":"192.168.0.100",
	    "ServiceID":"default:elated_swirles:8080",
	    "ServiceName":"my-service-name",
	    "ServiceTags":[
	      "tag1",
	      "tag2",
	      "uniqueContainerId1"
	    ],
	    "ServiceAddress":"192.168.99.100",
	    "ServicePort":32787
	  },
	  {
	    "Node":"my.consul.node",
	    "Address":"192.168.0.100",
	    "ServiceID":"default:elated_swirles:8443",
	    "ServiceName":"my-service-name",
	    "ServiceTags":[
	      "tag1",
	      "tag2",
	      "uniqueContainerId1"
	    ],
	    "ServiceAddress":"192.168.99.100",
	    "ServicePort":32786
	  },
	  {
	    "Node":"my.consul.node",
	    "Address":"192.168.0.100",
	    "ServiceID":"default:tender_brattain:8080",
	    "ServiceName":"my-service-name",
	    "ServiceTags":[
	      "tag1",
	      "tag2",
	      "uniqueContainerId2"
	    ],
	    "ServiceAddress":"192.168.99.100",
	    "ServicePort":32792
	  },
	  {
	    "Node":"my.consul.node",
	    "Address":"192.168.0.100",
	    "ServiceID":"default:tender_brattain:8443",
	    "ServiceName":"my-service-name",
	    "ServiceTags":[
	      "tag1",
	      "tag2",
	      "uniqueContainerId2"
	    ],
	    "ServiceAddress":"192.168.99.100",
	    "ServicePort":32791
	  }
	]
* 
* @author bitsofinfo
*
*/
public class OneServiceNameMultiPortStrategy extends ServiceNameStrategyBase implements ServiceNameStrategy {
	
	private static final Logger logger = LoggerFactory.getLogger(OneServiceNameMultiPortStrategy.class);


	public Collection<ServiceInfo> discover(CatalogClient catalogClient, 
											String serviceName, 
											Collection<Integer> ports,
											Collection<String> mustMatchTags) throws Exception {
		
		return super._discover(catalogClient, serviceName, ports, mustMatchTags);
		
	}


}
