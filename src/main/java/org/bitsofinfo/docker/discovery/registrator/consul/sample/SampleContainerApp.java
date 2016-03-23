package org.bitsofinfo.docker.discovery.registrator.consul.sample;

import java.util.Arrays;
import java.util.Collection;

import org.bitsofinfo.docker.discovery.registrator.consul.ConsulDiscovery;
import org.bitsofinfo.docker.discovery.registrator.consul.ServiceInfo;
import org.bitsofinfo.docker.discovery.registrator.consul.ServiceNameStrategy;

public class SampleContainerApp implements Runnable {
	
	public static void main(String args[]) throws Exception {
		SampleContainerApp app = new SampleContainerApp();
		
		Thread t = new Thread(app);
		t.start();
		
		Thread.currentThread().sleep(1000*60*30); // exit in 30 m
	}
	
	
	private String myUniqueTag;
	private ConsulDiscovery consulDiscovery = null;
	
	public SampleContainerApp() {
	
		try {
			myUniqueTag= System.getProperty("MY_UNIQUE_TAG");
			
			consulDiscovery = new ConsulDiscovery()
		            .setConsulIp(System.getProperty("CONSUL_IP"))
		            .setConsulPort(Integer.valueOf(System.getProperty("CONSUL_PORT")))
		            .setServiceName(System.getProperty("MY_SERVICE_NAME")) 
		            .setMyNodeUniqueTagId(myUniqueTag)
		            .addPortToDiscover(8080)
		            .addPortToDiscover(8443)
		            .setServiceNameStrategyClass(
		            		(Class<? extends ServiceNameStrategy>)
		            		Class.forName(System.getProperty("SERVICE_NAME_STRATEGY")));
			
		} catch(Exception e) {
			throw new RuntimeException("Error constructing SampleContainerApp " + e.getMessage(),e);
		}
	}


	@Override 
	public void run() {
		try {
			while(true) {
				Thread.currentThread().sleep(10000); 
			
				Collection<ServiceInfo> myServices = consulDiscovery.discoverMe();
				Collection<ServiceInfo> myPeerServices = consulDiscovery.discoverPeers();
				
				StringBuffer sb = new StringBuffer("########## " + myUniqueTag + " REPORTING: ##########\n");
				
				sb.append("MY SERVICES: \n");
				for (ServiceInfo info : myServices) {
					sb.append(info.getExposedAddress() + ":" + info.getExposedPort() + " -> container:"+ info.getMappedPort()+" tags: "+Arrays.asList(info.getTags().toArray())+"\n");
				}
				
				sb.append("\n\nMY PEER SERVICES: \n");
				for (ServiceInfo info : myPeerServices) {
					sb.append(info.getExposedAddress() + ":" + info.getExposedPort() + " -> container:"+ info.getMappedPort()+" tags: "+Arrays.asList(info.getTags().toArray())+"\n");
				}
				
				sb.append("\n########## END " + myUniqueTag + " ############\n\n");
				
				System.out.println(sb.toString());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
