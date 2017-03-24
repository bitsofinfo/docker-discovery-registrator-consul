package org.bitsofinfo.docker.discovery.registrator.consul;

import java.net.URL;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;


public class ConsulDiscoveryTest {

    @Test
    public void testConsulDiscovery() {
        
        try {
            ConsulDiscovery consulDiscovery = new ConsulDiscovery()
                                                    .setConsulUrl(new URL("http://192.168.0.148:8500"))
                                                    .setServiceName("consul") 
                                                    .setConsulAclToken("123")
                                                    .setMyNodeUniqueTagId("myuuid999999999999")
                                                    .addPortToDiscover(8500)
                                                    .addMustHaveTag("dev")
                                                    //.setServiceNameStrategyClass(OneServiceNameMultiPortStrategy.class);
                                                    .setServiceNameStrategyClass(MultiServiceNameSinglePortStrategy.class);
            
            Collection<ServiceInfo> allNodesServices = consulDiscovery.discoverAll();
            Collection<ServiceInfo> myServices = consulDiscovery.discoverMe();
            Collection<ServiceInfo> peerNodes = consulDiscovery.discoverPeers();
            

            Collection<ServiceInfo> myAkkaService = consulDiscovery.discoverMe(2552);
            Collection<ServiceInfo> peerAkkaNodes = consulDiscovery.discoverPeers(2552);
                                                    
            for (ServiceInfo info : allNodesServices) {
                System.out.println(info);
            }
        
        } catch(Exception e) {
        	e.printStackTrace();
            Assert.assertFalse(true);
            
        }
    }
    
}
