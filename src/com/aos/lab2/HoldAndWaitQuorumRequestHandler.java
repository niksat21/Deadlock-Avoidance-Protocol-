package com.aos.lab2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CircularQuorumRequestHandler {

	private static Logger logger = LogManager.getLogger(CircularWaitCSHandler.class);

	private List<CSRequest> requestQueue = new LinkedList<CSRequest>();
	private Node quorumNode;
	private Client client;
	private Config config;
	private Map<Integer, Integer> nodeIdVsPort = new HashMap<Integer, Integer>();

	public CircularQuorumRequestHandler(List<CSRequest> requestQueue, Node quorumNode, Client client, Config config,
			Map<Integer, Integer> nodeIdVsPort) {
		super();
		this.requestQueue = requestQueue;
		this.quorumNode = quorumNode;
		this.client = client;
		this.config = config;
		this.nodeIdVsPort = nodeIdVsPort;
	}

	public synchronized void handleRequestMessage(Integer source) {
		logger.debug("Received request message from nodeId:{} in quorum nodeId:{}", source, quorumNode.getNodeId());
		CSRequest request = new CSRequest(source);
		requestQueue.add(request);

		if (requestQueue.size() == 1) {
			sendGrantMessage(request.getNodeId());
		}
	}

	public synchronized void handleReleaseMessage(Integer source) {
		logger.debug("Received release message from nodeId:{} in quorum nodeId:{}", source, quorumNode.getNodeId());

		if (requestQueue.size() != 0) {
			CSRequest request = requestQueue.remove(0);
			sendGrantMessage(request.getNodeId());
		} else {
			logger.debug("No pending request in the quorum nodeId:{}", quorumNode.getNodeId());
		}
	}

	public void sendGrantMessage(Integer destinationId) {
		logger.debug("Sending grant message to nodeId:{} from  quorum nodeId:{}", destinationId,
				quorumNode.getNodeId());
		Message msg = new Message(quorumNode.getNodeId(), destinationId, MessageType.GRANT,
				nodeIdVsPort.get(destinationId));
		client.sendMsg(msg);
	}

}
