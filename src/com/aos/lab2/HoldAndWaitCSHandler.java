package com.aos.lab2;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HoldAndWaitCSHandler implements ICriticalSectionHandler {

	private static final Logger logger = LogManager.getLogger(HoldAndWaitCSHandler.class);
	private static final int DEFAULT_SLEEP_TIME = 100;
	private static final int RELEASE_WAIT = 2000;

	private Config config;
	private Node sourceNode;
	private Client client;
	private Set<Node> quorumSet;
	private Integer requestedNodeId = null;
	private volatile boolean waitingForReply = false;

	public HoldAndWaitCSHandler(Config config, Node sourceNode, Client client, Set<Node> quorumSet) {
		super();
		this.config = config;
		this.sourceNode = sourceNode;
		this.client = client;
		this.quorumSet = quorumSet;
	}

	@Override
	public void csEnter(Long timestamp) throws InterruptedException {
		// Send request message to all the nodes in the quorum set
		for (Node node : quorumSet) {
			Message msg = new Message(sourceNode.getNodeId(), node.getNodeId(), MessageType.REQUEST);
			logger.debug("Sending request message to nodeId:{} from nodeId:{}. Timestamp: {}", node.getNodeId(),
					sourceNode.getNodeId(), timestamp);
			client.sendMsg(msg);
			requestedNodeId = node.getNodeId();
			waitingForReply = true;
			while (true) {
				synchronized (this) {
					if (!waitingForReply) {
						break;
					}
				}
				Thread.sleep(DEFAULT_SLEEP_TIME);
			}
		}
		Thread.sleep(RELEASE_WAIT);
	}

	@Override
	public void csLeave() {
		Node[] nodes = new Node[quorumSet.size()];
		quorumSet.toArray(nodes);
		for (int i = nodes.length - 1; i >= 0; i--) {
			logger.debug("Sending release message to nodeId:{} from nodeId:{}", nodes[i].getNodeId(),
					sourceNode.getNodeId());
			client.sendMsg(new Message(sourceNode.getNodeId(), nodes[i].getNodeId(), MessageType.RELEASE));
		}
	}

	public synchronized void handleGrantMessage(Integer nodeId) {
		logger.debug("Received grant message by nodeId:{} from quorum nodeId:{}", sourceNode.getNodeId(), nodeId);
		waitingForReply = false;
		requestedNodeId = null;
	}

}
