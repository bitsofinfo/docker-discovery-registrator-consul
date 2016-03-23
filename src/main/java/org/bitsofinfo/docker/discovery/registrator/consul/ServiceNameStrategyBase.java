package org.bitsofinfo.docker.discovery.registrator.consul;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.CatalogService;

/**
 * Base code shared across all service name strategy impls. 
 * Does the bulk of the work for collecting all service information
 * for a given service name, interested ports and matching tags
 * 
 * @author bitsofinfo
 *
 */
public abstract class ServiceNameStrategyBase {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceNameStrategyBase.class);

	protected Collection<ServiceInfo> _discover(CatalogClient catalogClient, 
												String serviceName, 
												Collection<Integer> ports,
												Collection<String> mustMatchTags) throws Exception {
		
		List<ServiceInfo> discoveredServices = new ArrayList<ServiceInfo>();
		
		ConsulResponse<List<CatalogService>> resp = catalogClient.getService(serviceName);
		List<CatalogService> serviceList = resp.getResponse();
		
		for (CatalogService srv : serviceList) {
			
			if (matchesTags(srv.getServiceTags(),mustMatchTags)) {
				
				try {
					// we parse mapped port from serviceId format "xx:yy:port"
					// registrator sets the serviceId = to this format above for each
					// unique port
					int mappedPort = Integer.valueOf(srv.getServiceId().split(":")[2]);

					// if we care about this mapped port... capture the service 
					if (ports.contains(mappedPort)) {
						
						InetAddress exposedAddress = InetAddress.getByName(srv.getServiceAddress());
						ServiceInfo info = new ServiceInfo(srv.getServiceName(),
														    srv.getServiceId(),
														    exposedAddress,
														    srv.getServicePort(),
														    mappedPort,
														    srv.getServiceTags());
						discoveredServices.add(info);
						
						logger.debug("Discovered ServiceInfo: " + info);
					}
					
				} catch(Exception e) {
					throw new Exception("discover() Unexpected error processing service: " + srv.getServiceName() + " " + e.getMessage(),e);
				}
			}
			
		}
		
		return discoveredServices;
	}
	
	
	private boolean matchesTags(Collection<String> existingTags, Collection<String> mustMatchTags) {
		if (mustMatchTags != null && mustMatchTags.size() > 0) {
			return existingTags.containsAll(mustMatchTags);
		}
		return true;
	}
	
}
