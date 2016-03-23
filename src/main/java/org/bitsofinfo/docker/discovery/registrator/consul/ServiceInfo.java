package org.bitsofinfo.docker.discovery.registrator.consul;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;

/**
 * Simple pojo identifying a Service and info about it
 * 
 * @author bitsofinfo
 *
 */
public class ServiceInfo {
    
    private int exposedPort;
    private int mappedPort;
    private InetAddress exposedAddress; 
    private String serviceName;
    private String serviceId;
    private Collection<String> tags = null;
    
    public ServiceInfo(String serviceName, 
                       String serviceId,
                       InetAddress exposedAddress, 
                       int exposedPort, 
                       int mappedPort,
                       Collection<String> tags) {
        
        this.exposedAddress = exposedAddress;
        this.exposedPort = exposedPort;
        this.mappedPort = mappedPort;
        this.serviceName = serviceName;
        this.tags = tags;
        this.serviceId = serviceId;
        
    }

    public String getServiceId() {
        return this.serviceId;
    }
    
    public int getExposedPort() {
        return exposedPort;
    }

    public InetAddress getExposedAddress() {
        return exposedAddress;
    }

    public String getServiceName() {
        return serviceName;
    }
    
    public Collection<String> getTags() {
        return tags;
    }

    public int getMappedPort() {
        return mappedPort;
    }
    
    @Override
    public String toString() {
        return "{\"serviceName\":\""+ this.getServiceName() +"\"," +
               "\"serviceId\":\""+ this.getServiceId() +"\"," +
               "\"exposedAddress\":\""+ this.getExposedAddress().getHostAddress() +"\"," +
               "\"exposedPort\":"+ this.getExposedPort() +"," +
               "\"mappedPort\":"+ this.getMappedPort() +"," +
               "\"tags\":\""+ Arrays.toString(this.getTags().toArray()) +"\"}";
    }
    
}
