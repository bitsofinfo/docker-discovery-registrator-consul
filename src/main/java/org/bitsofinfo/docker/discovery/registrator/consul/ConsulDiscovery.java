package org.bitsofinfo.docker.discovery.registrator.consul;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.Consul.Builder;

/**
 * Use ConsulDiscovery for finding information about services/ports
 * and their bindings for a your application (which is calling this
 * and running in a docker container). That are registered in Consul
 * via Registrator
 * 
 * It assumes that each container you launch that uses this library
 * is given some sort of startup parameter which indicates a unique
 * value for the container (i.e. the 'myNodeUniqueTagId' constructor arg below)
 * that corresponds to a custom Registrator SERVICE_TAG you specify when running
 * the container via docker run -e SERVICE_TAGS="x,y,myNodeUniqueTagId" 
 * on a docker host with Registrator monitoring all launched containers and
 * reporting them to Consul
 * 
 * see: https://github.com/gliderlabs/registrator
 * see: http://gliderlabs.com/registrator/latest/
 * 
 * @author bitsofinfo
 *
 */
public class ConsulDiscovery {
    
    private static final Logger logger = LoggerFactory.getLogger(ConsulDiscovery.class);

    private String consulIp;
    private int consulPort;
    private String serviceName;
    private Collection<Integer> portsToDiscover = new ArrayList<Integer>();
    private Collection<String> mustHaveTags = new ArrayList<String>();
    private String myNodeUniqueTagId = null;
    
    private Class<? extends ServiceNameStrategy> serviceNameStrategyClass = null;
    
    /**
     * Constructor for builder syntax
     */
    public ConsulDiscovery(){}
    
    /**
     * Constructor when not using builder syntax
     * 
     * @param consulIp
     * @param consulPort
     * @param serviceName
     * @param myNodeUniqueTagId
     * @param portsToDiscover
     * @param mustHaveTags
     * @param serviceNameStrategyClass
     */
    public ConsulDiscovery(String consulIp, 
                          int consulPort, 
                          String serviceName,
                          String myNodeUniqueTagId,
                          Collection<Integer> portsToDiscover,
                          Collection<String> mustHaveTags,
                          Class<? extends ServiceNameStrategy> serviceNameStrategyClass) {
        super();
        
        if (mustHaveTags == null) {
            mustHaveTags = new ArrayList<String>();
        }
        this.mustHaveTags = mustHaveTags;
        
        this.consulIp = consulIp;
        this.consulPort = consulPort;
        this.serviceName = serviceName;
        this.portsToDiscover = portsToDiscover;
        this.serviceNameStrategyClass = serviceNameStrategyClass;
        this.myNodeUniqueTagId = myNodeUniqueTagId;
    }
    
    /**
     * Get all ServiceInfo objects that match our configuration for 
     * matching servicename, tags, ports AND tag "myNodeUniqueTagId"
     * 
     * @return
     * @throws Exception
     */
    public Collection<ServiceInfo> discoverMe() throws Exception {
        // get all with configured tags + our unique id which will
        // be only us.
        Collection<String> filters = new ArrayList<String>();
        filters.addAll(this.mustHaveTags);
        filters.add(this.myNodeUniqueTagId);
        return _discover(filters);
    }
    
    /**
     * Get all ServiceInfo objects that match our configuration for 
     * matching servicename, tags, ports AND tag "myNodeUniqueTagId". 
     * With specific mapped port filter
     * 
     * @param withMappedPort
     * @return
     * @throws Exception
     */
    public Collection<ServiceInfo> discoverMe(int withMappedPort) throws Exception {
        Collection<ServiceInfo> withPort = new ArrayList<ServiceInfo>();

        Collection<ServiceInfo> infos = discoverMe(); 
        
        for(ServiceInfo info : infos) {
            if (info.getMappedPort() == withMappedPort) {
                withPort.add(info);
            }
        }
        
        return withPort;
    }
    
    /**
     * Get all ServiceInfo objects that match our configuration for 
     * matching servicename, tags, ports OTHER THAN tag "myNodeUniqueTagId". 
     * 
     * @return
     * @throws Exception
     */
    public Collection<ServiceInfo> discoverPeers() throws Exception {
        Collection<ServiceInfo> peers = new ArrayList<ServiceInfo>();

        // get all, then filter out everything OTHER than our uniqueId
        Collection<ServiceInfo> infos = _discover(this.mustHaveTags);
        for(ServiceInfo info : infos) {
            if (!info.getTags().contains(this.myNodeUniqueTagId)) {
                peers.add(info);
            }
        }
        
        return peers;
    }
    
    /**
     * Get all ServiceInfo objects that match our configuration for 
     * matching servicename, tags, ports OTHER THAN tag "myNodeUniqueTagId". 
     * With specific mapped port filter
     * 
     * @param withMappedPort
     * @return
     * @throws Exception
     */
    public Collection<ServiceInfo> discoverPeers(int withMappedPort) throws Exception {
        Collection<ServiceInfo> withPort = new ArrayList<ServiceInfo>();
        
        Collection<ServiceInfo> infos = discoverPeers(); 
        
        for(ServiceInfo info : infos) {
            if (info.getMappedPort() == withMappedPort) {
                withPort.add(info);
            }
        }
        
        return withPort;
    }
    
    /**
     * Get all ServiceInfo objects that match our configuration for 
     * matching servicename, tags, ports (including ourself)

     * @return
     * @throws Exception
     */
    public Collection<ServiceInfo> discoverAll() throws Exception {
        return _discover(this.mustHaveTags); // all
    }
    

    /**
     * Get all ServiceInfo objects that match our configuration for 
     * matching servicename, tags, ports (including ourself)
     * 
     * With specific mapped port filter
     * 
     * @param withMappedPort
     * @return
     * @throws Exception
     */
    public Collection<ServiceInfo> discoverAll(int withMappedPort) throws Exception {
        Collection<ServiceInfo> withPort = new ArrayList<ServiceInfo>();
        
        Collection<ServiceInfo> infos = discoverAll(); // all
        
        for(ServiceInfo info : infos) {
            if (info.getMappedPort() == withMappedPort) {
                withPort.add(info);
            }
        }
        
        return withPort;
    }
    
    
    private Collection<ServiceInfo> _discover(Collection<String> mustHaveTags) throws Exception {
        
        // initialize the service name strategy
        ServiceNameStrategy serviceNameStrategy = null;
        try {
            serviceNameStrategy = this.serviceNameStrategyClass.newInstance();
            logger.debug("Using ServiceNameStrategy " + this.serviceNameStrategyClass.getSimpleName());
            
        } catch(Exception e) {
            throw new Exception("Unexpected error creating "
                    + "ServiceNameStrategy["+this.serviceNameStrategyClass.getName()+"]: " + e.getMessage(),e);
        }
        
        
        // create catalog client
        CatalogClient catalogClient = null;
        
        try {
            Builder consulBuilder = Consul.builder();
            Consul consul = consulBuilder.withHostAndPort(HostAndPort.fromParts(this.consulIp, this.consulPort)).build();
            catalogClient = consul.catalogClient();
            logger.debug("Configured to interrogate Consul @ " + this.consulIp + ":" + this.consulPort);
            
        } catch(Exception e) {
            throw new Exception("Unexpected error building Consul CatalogClient: " + e.getMessage(),e);
        }
        
        // invoke the actual discovery
        try {
            return serviceNameStrategy.discover(catalogClient, this.serviceName, this.portsToDiscover, mustHaveTags);
        } catch(Exception e) {
            throw new Exception("Unexpected error calling ServiceNameStrategy.discover() " + e.getMessage(),e);
        }
        
        
    }    

    public ConsulDiscovery setConsulIp(String consulIp) {
        this.consulIp = consulIp;
        return this;
    }


    public ConsulDiscovery setConsulPort(int consulPort) {
        this.consulPort = consulPort;
        return this;
    }


    public ConsulDiscovery setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }


    public ConsulDiscovery setPortsToDiscover(List<Integer> portsToDiscover) {
        this.portsToDiscover = portsToDiscover;
        return this;
    }
    
    public ConsulDiscovery addPortToDiscover(Integer portToDiscover) {
        this.portsToDiscover.add(portToDiscover);
        return this;
    }
    
    public ConsulDiscovery addMustHaveTag(String tag) {
        this.mustHaveTags.add(tag);
        return this;
    }


    public ConsulDiscovery setMyNodeUniqueTagId(String myNodeUniqueTagId) {
        this.myNodeUniqueTagId = myNodeUniqueTagId;
        return this;
    }


    public ConsulDiscovery setServiceNameStrategyClass(Class<? extends ServiceNameStrategy> serviceNameStrategyClass) {
        this.serviceNameStrategyClass = serviceNameStrategyClass;
        return this;
    }

    public ConsulDiscovery setMustHaveTags(Collection<String> mustHaveTags) {
        this.mustHaveTags = mustHaveTags;
        return this;
    }
    
    
}
