package org.bitsofinfo.docker.discovery.registrator.consul;

import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbitz.consul.CatalogClient;

/**
 * Strategy for Docker Registrator generated service names 
 * where the Registrator monitored Docker container was started 
 * by explicity setting a unique port based SERVCE_[PORT]_NAME as follows, 
 * or NOT SETTING any "SERVICE_NAME" environment at all (by which the default
 * with registration is to do [container-name]-[port]. 
 * 
 * * see: http://gliderlabs.com/registrator/latest/user/services/#service-name 
 * 
 * docker run -e "SERVICE_8080_NAME=my-service-name" -e "SERVICE_8443_NAME=my-service-name" -e "SERVICE_TAGS=tag1,tag2,myUniqueContainerId1" [CONTAINER_NAME|ID]
 * 
 * OR
 * 
 * docker run -e "SERVICE_TAGS=tag1,tag2,myUniqueContainerId1" [CONTAINER_NAME|ID]
 * 
 * Where [CONTAINER_NAME|ID] = "serviceName" as denoted in the constructor below 
 * 
 * 
 * i.e. in consul they are fetched as:
 * 
 * /v1/catalog/service/my-service-name-8080
 * /v1/catalog/service/my-service-name-8443
 * 
 * 
 * GET /v1/catalog/services
 * 
     {
      "consul":[
      ],
      "my-service-name-8080":[
        "tag1",
        "tag2",
        "uniqueContainerId1",
        "uniqueContainerId2"
      ],
      "my-service-name-8443":[
        "tag1",
        "tag2",
        "uniqueContainerId1",
        "uniqueContainerId2"
      ]
    }
    
 *
 * GET /v1/catalog/service/my-service-name-8080
 * 
    [
      {
        "Node":"my.consul.node",
        "Address":"192.168.0.100",
        "ServiceID":"default:admiring_mcclintock:8080",
        "ServiceName":"my-service-name-8080",
        "ServiceTags":[
          "tag1",
          "tag2",
          "uniqueContainerId1"
        ],
        "ServiceAddress":"192.168.99.100",
        "ServicePort":32770
      },
      {
        "Node":"my.consul.node",
        "Address":"192.168.0.100",
        "ServiceID":"default:agitated_franklin:8080",
        "ServiceName":"my-service-name-8080",
        "ServiceTags":[
          "tag1",
          "tag2"
          "uniqueContainerId2"
        ],
        "ServiceAddress":"192.168.99.100",
        "ServicePort":32775
      }
    ]

 * 
 * @author bitsofinfo
 *
 */
public class MultiServiceNameSinglePortStrategy extends ServiceNameStrategyBase implements ServiceNameStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiServiceNameSinglePortStrategy.class);
    
    public Collection<ServiceInfo> discover(CatalogClient catalogClient, 
                                            String serviceName,
                                            Collection<Integer> ports, 
                                            Collection<String> mustMatchTags) throws Exception {

        Collection<ServiceInfo> services = new ArrayList<ServiceInfo>();
        
        // for each port, we need to append the port to the serviceName base
        for (Integer port : ports) {
            services.addAll(
                    super._discover(catalogClient, (serviceName+"-"+port.toString()), ports, mustMatchTags)
                    );
            
        }
        
        return services;
        
    }

}
