package com.aos.lab2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestingCandidate {

	private static final Logger logger = LogManager.getLogger(RequestingCandidate.class);

	private Config config;
	private Integer nodeId;
	private Client client;
	private ICriticalSectionHandler csHandler;

	public RequestingCandidate(Config config, Integer nodeId, Client client, ICriticalSectionHandler csHandler) {
		this.config = config;
		this.nodeId = nodeId;
		this.client = client;
		this.csHandler = csHandler;
	}

	public void requestCS() throws InterruptedException {
		int count = 0;
		int noOfRequests = config.getNoOfAttempts();
		Node node = config.getNodeById(nodeId);
		// sleep for some random time before making request for CS
		Thread.sleep(getExpoRandom(config.getWaitTime()));

		while (count < noOfRequests) {
			logger.debug("noOfRequests :{} count:{} ", noOfRequests, count);
			long timestamp = System.currentTimeMillis();
			csHandler.csEnter(timestamp);
			logger.info("Critical Section: Enter NodeId:{} Request TS:{}", node.getNodeId(), timestamp);

			// sleep till CS is executed
			Thread.sleep(getExpoRandom(config.getCsExecTime()));
			csHandler.csLeave();
			logger.info("Critical Section: Leave NodeId:{}", node.getNodeId());
			count++;
		}
		//start broadcasting complete message
		ServerWorker.isCompleted = true;
		client.broadcastCompletionMsg();
	}

	private static int getExpoRandom(int mean) {

		double temp = Math.random();
		double exp = -(Math.log(temp) * mean);

		return (int) exp;

	}

}