package com.aos.lab2;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreemptiveCSHandler implements ICriticalSectionHandler {

	private static final Logger logger = LogManager.getLogger(PreemptiveCSHandler.class);
	private static final int DEFAULT_SLEEP_TIME = 100;

	private Config config;
	private Set<Integer> quorumSet;
	private Set<Integer> grantSet;
	private Set<Integer> failedSet;
	private Node sourceNode;
	private Client client;
	private boolean isInsideCS = false;

	public PreemptiveCSHandler(Config config, Node sourceNode, Client client, Set<Integer> quorumSet) {
		super();
		this.config = config;
		this.sourceNode = sourceNode;
		this.client = client;
		this.quorumSet = quorumSet;
		this.grantSet = new HashSet<Integer>();
		this.failedSet = new HashSet<Integer>();
	}

	@Override
	public void csEnter(Long timestamp) throws InterruptedException {
		// Send request message to all the nodes in the quorum set
		for (Integer nodeId : quorumSet) {
			Message msg = new Message(sourceNode.getNodeId(), nodeId, MessageType.REQUEST);
			logger.debug("Sending request message to nodeId:{} from nodeId:{}", nodeId, sourceNode.getNodeId(),
					timestamp);
			client.sendMsg(msg);
		}
		while (true) {
			synchronized (this) {
				// TODO: Check if the requesting node is also in quorum set. If
				// so, then check with size - 1
				if (failedSet.size() == 0 && grantSet.size() == quorumSet.size())
					break;
			}
			Thread.sleep(DEFAULT_SLEEP_TIME);
		}
		isInsideCS = true;
		logger.debug("Critical Section request granted for NodeId:{}", sourceNode.getNodeId());
	}

	@Override
	public void csLeave() {
		// Send release message to all the nodes in the quorum set
		for (Integer nodeId : quorumSet) {
			Message msg = new Message(sourceNode.getNodeId(), nodeId, MessageType.RELEASE);
			logger.debug("Sending release message to nodeId:{} from nodeId:{}", nodeId, sourceNode.getNodeId());
			client.sendMsg(msg);
		}
		isInsideCS = false;
	}

	public synchronized void handleFailedMessage(Integer nodeId) {
		logger.debug("Received failed message for request by nodeId:{} from quorum nodeId:{}", sourceNode.getNodeId(),
				nodeId);
		if (failedSet.contains(nodeId)) {
			logger.error("Something wrong in the nodeId:{} . Failed set already contains the nodeId:{}",
					sourceNode.getNodeId(), nodeId);
		}
		failedSet.add(nodeId);
	}

	public synchronized void handleInquireMessage(Integer nodeId) {
		logger.debug("Received inquire message by nodeId:{} from quorum nodeId:{}", sourceNode.getNodeId(), nodeId);
		if (isInsideCS) {
			logger.info("The nodeId:{} is already in the critical section. Not reliquishing", sourceNode.getNodeId());
		} else {
			if (failedSet.size() >= 1) {
				logger.debug(
						"Received failed message from at least 1 quorum member in nodeId:{} . So, sending yield message to quorum nodeId:{}",
						sourceNode.getNodeId(), nodeId);
				client.sendMsg(new Message(sourceNode.getNodeId(), nodeId, MessageType.YIELD));
				grantSet.remove(nodeId);
				failedSet.add(nodeId);
			}
		}
	}

	public synchronized void handleGrantMessage(Integer nodeId) {
		logger.debug("Received grant message by nodeId:{} from quorum nodeId:{}", sourceNode.getNodeId(), nodeId);
		if (grantSet.contains(nodeId)) {
			logger.error("Something wrong in the nodeId:{} . Grant set already contains the nodeId:{}",
					sourceNode.getNodeId(), nodeId);
		}
		grantSet.add(nodeId);
		failedSet.remove(nodeId);
	}

}
