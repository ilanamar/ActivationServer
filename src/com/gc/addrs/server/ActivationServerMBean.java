/*
 * Created on Sep 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.gc.addrs.server;


/**
 * @author ilan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface ActivationServerMBean {

	public void createBridge(String bridgeType, String instanceId, boolean collectStats, boolean launchBridge);
	public void removeBridge(String bridgeType, String instanceId);

	public void createBridge2Nagra();
	public void removeBridge2Nagra();
	public void createBridge2NagraFeedback();
	public void removeBridge2NagraFeedback();
	public void createBridge2Nds();
	public void removeBridge2Nds();
	public void createBridge2NdsFeedback();
	public void removeBridge2NdsFeedback();
	
}
