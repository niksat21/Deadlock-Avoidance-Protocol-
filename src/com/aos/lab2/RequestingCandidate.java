package com.aos.lab2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestingCandidate {

	private static final Logger logger = LogManager.getLogger(RequestingCandidate.class);

	private Config config;
	private Integer nodeId;
	private Client client;

	public RequestingCandidate(Config config, Integer nodeId, Client client) {
		this.config = config;
		this.nodeId = nodeId;
		this.client = client;
	}

	public void requestCS() throws InterruptedException {
		DeadlockResolverType version = config.getVersion();
		int count = 0;
		int noOfRequests = config.getNoOfAttempts();
		Node node = config.getNodeById(nodeId);
		// sleep for some random time before making request for CS
		Thread.sleep(getExpoRandom(config.getWaitTime()));
		if (version.equals(DeadlockResolverType.PREEMPTIVE)) {
			while (count <= noOfRequests) {
				PreemptiveCSHandler pcsh = new PreemptiveCSHandler(config, node, client,
						config.getNodeQuorumById(nodeId));
				pcsh.csEnter(System.currentTimeMillis());
				logger.info("Critical Section: Enter NodeId:{}", node.getNodeId());
				// sleep till CS is executed
				Thread.sleep(getExpoRandom(config.getCsExecTime()));
				pcsh.csLeave();
				logger.info("Critical Section: Leave NodeId:{}", node.getNodeId());
				count++;
			}
		} else if (version.equals(DeadlockResolverType.HOLD_AND_WAIT)) {
			while (count <= noOfRequests) {
				HoldAndWaitCSHandler hwcsh = new HoldAndWaitCSHandler(config, node, client,
						config.getNodeQuorumById(nodeId));
				hwcsh.csEnter(System.currentTimeMillis());
				logger.info("Critical Section: Enter NodeId:{}", node.getNodeId());
				// sleep till CS is executed
				Thread.sleep(getExpoRandom(config.getCsExecTime()));
				hwcsh.csLeave();
				logger.info("Critical Section: Leave NodeId:{}", node.getNodeId());
				count++;
			}
		}

	}

	private static int getExpoRandom(int mean) {

		double temp = Math.random();
		double exp = -(Math.log(temp) * mean);

		return (int) exp;

	}

}