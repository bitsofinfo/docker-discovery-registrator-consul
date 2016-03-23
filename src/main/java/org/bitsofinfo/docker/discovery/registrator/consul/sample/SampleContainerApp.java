package org.bitsofinfo.docker.discovery.registrator.consul.sample;

import java.util.Arrays;
import java.util.Collection;

import org.bitsofinfo.docker.discovery.registrator.consul.ConsulDiscovery;
import org.bitsofinfo.docker.discovery.registrator.consul.ServiceInfo;
import org.bitsofinfo.docker.discovery.registrator.consul.ServiceNameStrategy;

/**
 * Sample app for learning how to use this library.
 * 
 * When launched in a container as noted in the README.md
 * file. This application uses the ConsulDiscovery class to
 * interrogate Consul for the information in it as created
 * by the Registrator container. Once obtained you can 
 * make as many calls as you would like to retrieve information
 * about yourself and your peers with the same shared service name
 * 
 * @author bitsofinfo
 *
 */
public class SampleContainerApp implements Runnable {

	/**
	 * Start the app with a background thread that reports state
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		SampleContainerApp app = new SampleContainerApp();

		Thread t = new Thread(app);
		t.start();

		Thread.sleep(1000*60*30); // exit in 30 m
	}


	private String myUniqueTag;
	private ConsulDiscovery consulDiscovery = null;

	/**
	 * Constructor
	 */
	@SuppressWarnings("unchecked")
	public SampleContainerApp() {

		try {
			/*
			 * All the System variables below are obtained via
			 * -D System property flags set on the JVM when the 
			 * app is launched. See example in README.
			 * 
			 * Note this is not the only way you can do this
			 * as there are numerous ways to get this information 
			 * into a container. This is just an example
			 */
			myUniqueTag= System.getProperty("MY_UNIQUE_TAG");

			// construct our ConsulDiscovery using builder methods
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
				Thread.sleep(10000); 

				// Lets discover ServiceInfo about myself 
				Collection<ServiceInfo> myServices = consulDiscovery.discoverMe();
				
				// and my peers....
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
