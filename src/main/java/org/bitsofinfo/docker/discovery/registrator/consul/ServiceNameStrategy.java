package org.bitsofinfo.docker.discovery.registrator.consul;

import java.util.Collection;

import com.orbitz.consul.CatalogClient;

/**
 * Does the work of actual discovery following a particular ServiceName "strategy"
 * as defined by the implementation according to the different Registration service
 * name behaviors as defined at: http://gliderlabs.com/registrator/latest/user/services/#service-name
 * 
 * @author bitsofinfo
 *
 */
public interface ServiceNameStrategy {
    
    public Collection<ServiceInfo> discover(CatalogClient catalogClient,
                                              String serviceName,
                                              Collection<Integer> ports,
                                              Collection<String> mustMatchTags) throws Exception;
    
}
