package com.aos.lab2;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreemptiveQuorumRequestHandler {

	private static final Logger logger = LogManager.getLogger(PreemptiveQuorumRequestHandler.class);

	private RequestTSComparator requestComparator = new RequestTSComparator();
	private PriorityQueue<CSRequest> queue;
	private Node quorumNode;
	private Client client;
	private Config config;
	private Map<Integer, Integer> nodeIdVsPort = new HashMap<Integer, Integer>();

	// TODO: Check if this has to be synchronized
	private boolean hasGranted = false;
	private CSRequest grantedRequest = null;

	public PreemptiveQuorumRequestHandler(Node quorumNode, Client client, Config config) {
		super();
		this.quorumNode = quorumNode;
		this.queue = new PriorityQueue<CSRequest>(requestComparator);
		this.client = client;
		this.config = config;

		for (Node node : config.getNodes()) {
			nodeIdVsPort.put(node.getNodeId(), node.getPort());
		}
	}

	public synchronized void handleRequest(CSRequest request) {
		CSRequest previousReq = queue.peek();

		// Add request to the queue
		queue.add(request);

		if (queue.peek() == request) {
			// New request has to be satisfied first, since it is at the top of
			// the queue.
			if (!hasGranted) {
				hasGranted = true;
				grantedRequest = request;
				sendGrantMessage(request);
			} else {
				// Send inquire message
				sendInquireMessage(request, previousReq);
			}
		} else {
			// Some other request has a lesser timestamp. So, that has to be
			// granted first.
			sendFailedMessage(request);
		}

	}

	public synchronized void handleYieldMessage(Integer sourceNodeId) {
		logger.info("Received yield message from nodeId:{} in quorum nodeId:{}", sourceNodeId, quorumNode.getNodeId());

		if (grantedRequest.getNodeId() != sourceNodeId) {
			logger.error(
					"Something is wrong in quorum nodeId:{}. Received an yield message from nodeId:{} but the request was granted to nodeId:{}",
					quorumNode.getNodeId(), sourceNodeId, grantedRequest.getNodeId());
		}

		if (queue.isEmpty()) {
			logger.error(
					"Something is wrong in quorum nodeId:{}. Received an yield message and the request queue is empty",
					quorumNode.getNodeId());
		} else {
			CSRequest request = queue.peek();
			hasGranted = true;
			grantedRequest = request;
			sendGrantMessage(request);
		}
	}

	public synchronized void handleReleaseMessage(Integer sourceNode, Message msg) {
		logger.info("Received release message from nodeId:{} in quorum nodeId:{}", sourceNode, quorumNode.getNodeId());

		if (!queue.remove(grantedRequest))
			logger.error(
					"Unable to find the request from nodeId:{} with TS:{} in the quorum nodeId:{} when handling release message",
					grantedRequest.getNodeId(), grantedRequest.getTimestamp(), quorumNode.getNodeId());

		if (grantedRequest.getNodeId() != sourceNode) {
			logger.error(
					"The nodeId:{} from which release message is obtained is not same as the nodeId:{} to which request was granted in the quorum nodeId:{}",
					sourceNode, grantedRequest.getNodeId());
		}

		if (!queue.isEmpty()) {
			// Send grant message to the next request
			CSRequest request = queue.remove();
			sendGrantMessage(request);
		} else {
			hasGranted = false;
			grantedRequest = null;
			logger.debug("No other request to be satisfied from quorum nodeId:{}", quorumNode.getNodeId());
		}

	}

	private void sendInquireMessage(CSRequest request, CSRequest previousReq) {
		Message msg = new Message(quorumNode.getNodeId(), previousReq.getNodeId(), MessageType.INQUIRE,
				nodeIdVsPort.get(previousReq.getNodeId()));
		logger.info(
				"Sending inquire message to nodeId:{} from quorum nodeId:{} as request with TS:{} from nodeId:{} has to be serviced",
				previousReq.getNodeId(), quorumNode.getNodeId(), request.getTimestamp(), request.getNodeId());
		client.sendMsg(msg);
	}

	private void sendGrantMessage(CSRequest request) {
		Message msg = new Message(quorumNode.getNodeId(), request.getNodeId(), MessageType.GRANT,
				nodeIdVsPort.get(request.getNodeId()));
		logger.info("Sending grant message to the requesting nodeId:{} from the quorum nodeId:{} .Request TS:{}",
				request.getNodeId(), quorumNode.getNodeId(), request.getTimestamp());
		client.sendMsg(msg);
	}

	private void sendFailedMessage(CSRequest request) {
		Message msg = new Message(quorumNode.getNodeId(), request.getNodeId(), MessageType.FAILED,
				nodeIdVsPort.get(request.getNodeId()));
		logger.info("Sending failed message to the requesting nodeId:{} from quorum nodeId:{}", request.getNodeId(),
				quorumNode.getNodeId());
		client.sendMsg(msg);
	}

}
