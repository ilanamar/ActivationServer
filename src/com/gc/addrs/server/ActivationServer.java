package com.gc.addrs.server;

import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.monitor.CounterMonitor;
import javax.management.monitor.MonitorNotification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.http.handler.SetResponseHeadersHandler;
import org.mortbay.jetty.Server;

import com.gc.addrs.Bridge;
import com.gc.addrs.config.BridgeGlobals;
import com.gc.addrs.nagra.Bridge2Nagra;
import com.gc.addrs.nagra.Bridge2NagraFeedback;
import com.gc.addrs.nds.Bridge2Nds;
import com.gc.addrs.nds.Bridge2NdsFeedback;
import com.gc.addrs.stat.BridgeStats;
import com.gc.addrs.util.SmtpAuthMailer;
// import com.sun.jdmk.comm.CommunicatorServer;
// import com.sun.jdmk.comm.HtmlAdaptorServer;

public class ActivationServer implements ActivationServerMBean, NotificationListener {

	public static final String JMX_DOMAIN_NAME = "HOT";
	
	private static final Log logger = LogFactory.getLog(ActivationServer.class);

	private MBeanServer mbeanServer = null;
	private Server downloadServer = null;
	private BridgeStatsReporter statsReporter = null;
	private Map bridges = new HashMap();
	private int httpPort = 8082;

	public ActivationServer() {
	}

	public void start() {
	
		try {
			initHttpDownloadServer();			
		} catch (Exception e) {
			logger.warn("Failed to start embedded Jetty HTTP server", e);
		}

		initJMXAgent();		
		
		String mbeanName = "ActivationServer";
		logger.debug("Register " + mbeanName + " management object");
		ObjectName mbeanObjectName = null;
		try {			
			mbeanObjectName = new ObjectName(":name=" + mbeanName);
			mbeanServer.registerMBean(this, mbeanObjectName);
		} catch (Exception e) {
			logger.warn("Could not register " + mbeanName + " management object", e);
		}

		int statsCollectInterval = BridgeGlobals.getInstance().getStatsCollectInterval();
		if (statsCollectInterval > 0) {
			this. statsReporter = new BridgeStatsReporter(statsCollectInterval);
			statsReporter.start();
			logger.info("Started Statistics-Reporter to collect performance metrics each " + statsCollectInterval + " minutes.");
			// threads.put(statsReporter.getName(), statsReporter);			
		} else {
			logger.info("Avoid starting Statistics-Reporter; collection interval = " + statsCollectInterval);			
		}
		
		while (true) {
//			if (BridgeGlobals.getInstance().isLaunchBridge2Nagra()) {
				createBridge2Nagra();
				createBridge2NagraFeedback();			
				BridgeGlobals.getInstance().setLaunchBridge2Nagra(false);
//			}
//			if (BridgeGlobals.getInstance().isLaunchBridge2Nds()) {
				createBridge2Nds();
				createBridge2NdsFeedback();
				BridgeGlobals.getInstance().setLaunchBridge2Nds(false);
//			}
			try { Thread.sleep(1000 * 1); } catch (Exception e) {}
		}
	}

	private void initHttpDownloadServer() throws Exception {
		downloadServer = new Server();
		int port = httpPort + 1;
		SocketListener localListener = new SocketListener();
		localListener.setPort(port);
		localListener.setBufferSize(1024 * 1000 * 1);
		downloadServer.addListener(localListener);				
		logger.info("Created HTTP listener: " + localListener);		
		HttpContext context = new HttpContext();
		context.setContextPath("/");
		context.setResourceBase("./");
		ResourceHandler resourceHandler = new ResourceHandler();
		SetResponseHeadersHandler setResponseHeadersHandler = new SetResponseHeadersHandler();
		setResponseHeadersHandler.setHeaderValue("Expires", "0");
		setResponseHeadersHandler.setHeaderValue("Pragma", "no-cache");
		setResponseHeadersHandler.setHeaderValue("Cache-Control", "no-cache");
		// setResponseHeadersHandler.setHeaderValue("Last-Modified", "Tue, 15 Nov 2000 12:45:26 GMT");
		// setResponseHeadersHandler.setHeaderValue("Last-Modified", "00000000000000000000000000000");
		context.addHandler(setResponseHeadersHandler);
		context.addHandler(resourceHandler);
		context.setMaxCachedFileSize(0);
		context.setMaxCacheSize(0);
		downloadServer.addContext(context);
		downloadServer.start();			
	}
	
	private void initJMXAgent() {
		logger.debug("Creating the JMX MBeanServer...");
		mbeanServer = MBeanServerFactory.createMBeanServer();
		logger.debug("Instantiating an HTML protocol adaptor with default port...");
		// addJDMKAdaptorsAndConnectors();
		addMX4JAdaptorsAndConnectors();
	}

//	private void addJDMKAdaptorsAndConnectors() {
//
//		// Adding an HTML protocol adaptor
//		// default constructor: sets port to default 8082
//		CommunicatorServer htmlAdaptor = new HtmlAdaptorServer();
//		// logger.debug("Adding an HTML protocol adaptor;" + " protocol= " + htmlAdaptor.getProtocol() + " port = " + htmlAdaptor.getPort());
//
//		// logger.debug("Registering the html protocol adaptor, with default name, in the MBean server...");
//		try {
//			// We let the html protocol adaptor provides its default name
//			ObjectInstance htmlAdaptorInstance = mbeanServer.registerMBean(htmlAdaptor, null);
//			// logger.debug("CLASS NAME = " + htmlAdaptorInstance.getClassName());
//			// logger.debug("OBJECT NAME = " + htmlAdaptorInstance.getObjectName().toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//
//		// Now we explicitly start the html protocol adaptor as it is not
//		// started automatically
//
//		logger.debug("Starting the HTML protocol adaptor...");
//		htmlAdaptor.start();
//
//		// waiting to leave starting state...
//		while (htmlAdaptor.getState() == CommunicatorServer.STARTING) {
//			logger.debug("HTML protocol adaptor STATE = " + htmlAdaptor.getStateString());
//			try { Thread.sleep(1000); } catch (Exception e) {}
//		}
//		logger.debug("HTML protocol adaptor STATE = " + htmlAdaptor.getStateString());
//
//		// Check that the HTML adaptor server is started
//		if (htmlAdaptor.getState() != CommunicatorServer.ONLINE) {
//			logger.fatal("Cannot start the HTML adaptor server, check that the port is not already used");
//			System.exit(1);
//		}
//
//		logger.debug("Startup of html protocol adaptor completed.");
//
//	} // end startConnectorsAndAdaptors()

	private void addMX4JAdaptorsAndConnectors() {
		
		boolean cachePages = true;
		String processorPath = "./xsl";
		String processorJar = null;
		String defaultPage = null; 
		
		try {
			ObjectName mbeanServerName = new ObjectName("Http:name=HttpAdaptor");
			mbeanServer.createMBean("mx4j.tools.adaptor.http.HttpAdaptor", mbeanServerName, null);
			
			// set attributes
			mbeanServer.setAttribute(mbeanServerName, new Attribute("Port", new Integer(httpPort)));
			mbeanServer.setAttribute(mbeanServerName, new Attribute("Host", "0.0.0.0"));

			// set the XSLTProcessor. If you want to use pure XML comment this out
			ObjectName processorName = new ObjectName("Http:name=XSLTProcessor");
			mbeanServer.createMBean("mx4j.tools.adaptor.http.XSLTProcessor", processorName, null);
			if (processorPath != null) {
				mbeanServer.setAttribute(processorName, new Attribute("File", processorPath));
			}
			mbeanServer.setAttribute(processorName, new Attribute("UseCache", new Boolean(cachePages)));
			if (processorJar != null) {
				mbeanServer.setAttribute(processorName, new Attribute("PathInJar", processorJar));
			}
			if (defaultPage != null) {
				mbeanServer.setAttribute(processorName, new Attribute("DefaultPage", defaultPage));
			}
			mbeanServer.setAttribute(mbeanServerName, new Attribute("ProcessorName", processorName));

			if (BridgeGlobals.getInstance().isConsoleLoginRequired()) {
				// add user names
				String username = BridgeGlobals.getInstance().getConsoleUsername();
				String password = BridgeGlobals.getInstance().getConsolePassword();				
				mbeanServer.invoke(
					mbeanServerName, 
					"addAuthorization", 
					new Object[] { username, password }, new String[] { "java.lang.String", "java.lang.String" });

				// use basic authentication
				mbeanServer.setAttribute(mbeanServerName, new Attribute("AuthenticationMethod", "basic"));				
			}

			// starts the mbeanServer
			mbeanServer.invoke(mbeanServerName, "start", null, null);
			
		} catch (Exception e) {
			logger.fatal("Cannot create or start the HTML adaptor server, check that the port is not already used", e);
			System.exit(1);
		}

		logger.debug("Startup of html protocol adaptor completed.");
	}

	private void createStandardBean(
			String mbeanClassName,
			ObjectName mbeanObjectName, 
			String mbeanName) throws Exception {

		logger.debug("Creating the " + mbeanName + " MBean within the MBeanServer:");
		mbeanServer.createMBean(mbeanClassName, mbeanObjectName);

	} // end of createStandardMBean()

	/* (non-Javadoc)
	 * @see com.gc.addrs.server.ActivationServerMBean#createBridge2Nagra()
	 */
	public void createBridge2Nagra() {
		createBridge("Bridge2Nagra", "", true, BridgeGlobals.getInstance().isLaunchBridge2Nagra());
	}

	/* (non-Javadoc)
	 * @see com.gc.addrs.server.ActivationServerMBean#removeBridge2Nagra()
	 */
	public void removeBridge2Nagra() {		
		removeBridge("Bridge2Nagra", "");
	}

	/* (non-Javadoc)
	 * @see com.gc.addrs.server.ActivationServerMBean#createBridge2NagraFeedback()
	 */
	public void createBridge2NagraFeedback() {
		createBridge("Bridge2NagraFeedback", "", false, BridgeGlobals.getInstance().isLaunchBridge2Nagra());
	}

	/* (non-Javadoc)
	 * @see com.gc.addrs.server.ActivationServerMBean#removeBridge2NagraFeedback()
	 */
	public void removeBridge2NagraFeedback() {
		removeBridge("Bridge2NagraFeedback", "");
	}

	/* (non-Javadoc)
	 * @see com.gc.addrs.server.ActivationServerMBean#createBridge2Nds()
	 */
	public void createBridge2Nds() {
		createBridge("Bridge2Nds", "", true, BridgeGlobals.getInstance().isLaunchBridge2Nds());
	}

	/* (non-Javadoc)
	 * @see com.gc.addrs.server.ActivationServerMBean#removeBridge2Nds()
	 */
	public void removeBridge2Nds() {
		removeBridge("Bridge2Nds", "");		
	}

	/* (non-Javadoc)
	 * @see com.gc.addrs.server.ActivationServerMBean#createBridge2NdsFeedback()
	 */
	public void createBridge2NdsFeedback() {
		createBridge("Bridge2NdsFeedback", "", false, BridgeGlobals.getInstance().isLaunchBridge2Nds());
	}

	/* (non-Javadoc)
	 * @see com.gc.addrs.server.ActivationServerMBean#removeBridge2NdsFeedback()
	 */
	public void removeBridge2NdsFeedback() {
		removeBridge("Bridge2NdsFeedback", "");
	}

	public void createBridge(String bridgeType, String instanceId, boolean collectStats, boolean launchBridge) {
		
		Bridge bridge = null;
		String bridgeName = bridgeType;
		if ((instanceId != null) && (instanceId.length() > 0)) {
			bridgeName = bridgeName + "." + instanceId;
		}
		
		bridge = (Bridge) bridges.get(bridgeName);
		
		if (bridge != null) {
			if (!isBridgeRegistered(bridgeName)) {
				logger.debug("Re-register " + bridgeName + "...");
				bridge = null;
			} else {
				return;
			}
		}

		if (bridgeType.equalsIgnoreCase("Bridge2Nagra")) {
			bridge = new Bridge2Nagra(bridgeName);
		} else if (bridgeType.equalsIgnoreCase("Bridge2Nds")) {
			bridge = new Bridge2Nds(bridgeName);
		} else if (bridgeType.equalsIgnoreCase("Bridge2NagraFeedback")) {
			bridge = new Bridge2NagraFeedback(bridgeName);
		} else if (bridgeType.equalsIgnoreCase("Bridge2NdsFeedback")) {
			bridge = new Bridge2NdsFeedback(bridgeName);
		} else {
			throw new IllegalArgumentException("invalid bridge type: " + bridgeType);
		}
				
		bridges.put(bridge.getName(), bridge);
		
		logger.debug("Register " + bridgeName + " management object");
		ObjectName mbeanObjectName = null;
		try {			
			mbeanObjectName = new ObjectName(JMX_DOMAIN_NAME + ":name=" + bridgeName);
			mbeanServer.registerMBean(bridge, mbeanObjectName);
			
//			// register dynamic MBean
//			String mbeanClassName = "javax.management.modelmbean.RequiredModelMBean";			
//			// set management interface in ModelMBean - attributes, operations, notifications
//			ModelMBeanInfo dMBeanInfo = bridge.buildDynamicMBeanInfo(mbeanObjectName, mbeanName);
//		    RequiredModelMBean modelmbean = new RequiredModelMBean(dMBeanInfo);
//		    // Set the managed resource for ModelMBean instance
//		    modelmbean.setManagedResource(bridge, "objectReference");
//		    // register the ModelMBean in the MBean Server
//			mbeanServer.registerMBean(modelmbean, mbeanObjectName);
		
			bridge.setMbeanServer(mbeanServer);
			bridge.setMbeanObjectName(mbeanObjectName);

		} catch (Exception e) {
			logger.error("Could not register " + bridgeName + " management object", e);
		}
		
		if (collectStats) {
			BridgeStats bridgeStat = bridge.getStats();
//			mbeanName = bridgeName + "Statistics";
//			logger.debug("Register " + mbeanName + " management object");
//			try {			
//				mbeanObjectName = new ObjectName(domain + ":name=" + mbeanName);
//				mbeanServer.registerMBean(bridgeStat, mbeanObjectName);
//			} catch (Exception e) {
//				logger.warn("Could not register " + mbeanName + " management object", e);
//			}
			statsReporter.addStatObject(bridgeStat);			
		}
		
		if (launchBridge) {
			bridge.startBridge();		
		}

		// Add counter monitor for Number of Pending Requests
		if ((bridge instanceof Bridge2Nagra) || (bridge instanceof Bridge2Nds)) {
	        
			CounterMonitor counterMonitor = new CounterMonitor();
	        Integer threshold = new Integer(BridgeGlobals.getInstance().getPendingRequestsThreshold());
	        Integer offset  = new Integer(0);

	        try {
	            counterMonitor.addObservedObject(mbeanObjectName);
	            counterMonitor.setObservedAttribute("PendingRequests");
	            counterMonitor.setNotify(true);
	            counterMonitor.setInitThreshold(threshold);
	            // counterMonitor.setOffset(offset);
	            counterMonitor.setGranularityPeriod(10000);

	            // create a new CounterMonitor MBean
                ObjectName counterMonitorName = new ObjectName("Monitor:name=MonitorPendingRequests." + bridgeName);
                mbeanServer.registerMBean(counterMonitor, counterMonitorName);

	            // Register a CounterMonitor notification listener with the CounterMonitor MBean,
	            // enabling the Agent to receive CounterMonitor notification emitted by the CounterMonitor. 
	            counterMonitor.addNotificationListener(this, null, null);
		        counterMonitor.start();			
	        } catch (InstanceAlreadyExistsException iaee) {
	        	// ignore
	        } catch (Exception e) {
				logger.error("Failed to create PendingRequests Attribute Monitor for " + bridgeName + " management object", e);
	        }

	        CounterMonitor counterMonitor2 = new CounterMonitor();
	        threshold = new Integer(BridgeGlobals.getInstance().getFailedRequestsThreshold());
	        offset  = new Integer(BridgeGlobals.getInstance().getFailedRequestsThreshold());

	        try {
	            counterMonitor2.addObservedObject(mbeanObjectName);
	            counterMonitor2.setObservedAttribute("PacketsFailed");
	            counterMonitor2.setNotify(true);
	            counterMonitor2.setInitThreshold(threshold);
	            counterMonitor2.setOffset(offset);
	            counterMonitor2.setGranularityPeriod(10000);

	            // create a new CounterMonitor MBean
                ObjectName counterMonitorName = new ObjectName("Monitor:name=MonitorFailedRequests." + bridgeName);
                mbeanServer.registerMBean(counterMonitor2, counterMonitorName);

	            // Register a CounterMonitor notification listener with the CounterMonitor MBean,
	            // enabling the Agent to receive CounterMonitor notification emitted by the CounterMonitor. 
	            counterMonitor2.addNotificationListener(this, null, null);
		        counterMonitor2.start();			
	        } catch (InstanceAlreadyExistsException iaee) {
	        	// ignore
	        } catch (Exception e) {
				logger.error("Failed to create FailedRequests Attribute Monitor for " + bridgeName + " management object", e);
	        }
		}
        
	}

	/**
	 * @return
	 */
	private boolean isBridgeRegistered(String bridgeName) {
		boolean ret = false;
		try {			
			ObjectName mbeanObjectName = new ObjectName(JMX_DOMAIN_NAME + ":name=" + bridgeName);
			ret = mbeanServer.isRegistered(mbeanObjectName);
		} catch (Exception e) {
		}
		return ret;
	}

	public void removeBridge(String bridgeType, String instanceId) {
		
		String threadName = bridgeType + "." + instanceId;
		Bridge bridge = (Bridge) bridges.get(threadName);
		
		if (bridge == null) {
			logger.debug("Bridge2Nagra was not created - remove request ignored");
		}
		logger.debug("Removing " + threadName + "...");

		bridge.stopBridge();
		
		String mbeanName = threadName;
		logger.debug("Unregister " + mbeanName + " management object");
		ObjectName mbeanObjectName = null;
		try {			
			mbeanObjectName = new ObjectName(JMX_DOMAIN_NAME + ":name=" + mbeanName);
			mbeanServer.unregisterMBean(mbeanObjectName);
		} catch (Exception e) {
			logger.warn("Could not unregister " + mbeanName + " management object", e);
		}
		
		// BridgeStatsReporter statsReporter = (BridgeStatsReporter)bridges.get("BridgeStatsReporter");
		statsReporter.removeStatObject(bridge.getStats());

		logger.debug(threadName + " removed");
	}

	/**
	 * 
	 */
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public void handleNotification(Notification notification, Object handback) {
		
		MonitorNotification notif = (MonitorNotification) notification;

		// Process the different types of notifications fired by the CounterMonitor.
		String type = notif.getType();
		
		try {
		
			if (type.equals(MonitorNotification.THRESHOLD_VALUE_EXCEEDED)) {
				
				String bridgeName = notif.getObservedObject().getKeyProperty("name");
				String msg = bridgeName + " - " + notif.getObservedAttribute() + " has reached threshold: " + notif.getMessage(); 
				logger.warn(msg);		
				
				if (BridgeGlobals.getInstance().isEnableEmailAlerts()) {
					try {
						SmtpAuthMailer mailer = new SmtpAuthMailer(
								BridgeGlobals.getInstance().getSmptHost(), 
								BridgeGlobals.getInstance().getSmtpUsername(), 
								BridgeGlobals.getInstance().getSmtpPassword());
					
						StringBuffer sb = new StringBuffer();
						sb.append("\n");
						sb.append(msg);
						sb.append("\n");
						sb.append("Target system may be OFFLINE or not responding correctly !");			
						sb.append("\n");
					
						mailer.sendMail(
								BridgeGlobals.getInstance().getAlertFrom(), 
								BridgeGlobals.getInstance().getAlertEmailRecipients(), 
								BridgeGlobals.getInstance().getAlertSubject(), 
								sb.toString(),
								null);		
					}
					catch (Exception e1) {
						logger.error("Failed to send monitor alert, reason: " + e1.getMessage());
					}					
				}
				
			} else {
				logger.error("Unknown event type; stopping the CounterMonitor");
				try {
					CounterMonitor monitor = (CounterMonitor) notif.getSource();
					monitor.stop();
				} catch (Exception ignore) {
				}
			}
		} catch (Exception e) {
			logger.error("Failed to handle Monitor notification; stopping the CounterMonitor", e);
			try {
				CounterMonitor monitor = (CounterMonitor) notif.getSource();
				monitor.stop();
			} catch (Exception ignore) {
			}
		}
	}

	public static void main(String[] args) {
		ActivationServer server = new ActivationServer();
		Thread shutdownHook = new ActivationServerShutdownHook(server);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		server.start();
		
	} // end of main()
	
}

class ActivationServerShutdownHook extends Thread {
	
	private ActivationServer activatonServer;
	
	public ActivationServerShutdownHook(ActivationServer activatonServer) {
		this.activatonServer = activatonServer;
	}
	
    public void run() {
        System.out.println("ActivationServer shutdown hook executing...");
    	activatonServer.stop();
        System.out.println("ActivationServer shutdown hook done");
    }
	
}
