package com.aos.lab2;

import java.util.PriorityQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QuorumRequestHandler {

	private static final Logger logger = LogManager.getLogger(QuorumRequestHandler.class);

	private RequestTSComparator requestComparator = new RequestTSComparator();
	private PriorityQueue<CSRequest> queue;
	private Node quorumNode;
	private Client client;

	// TODO: Check if this has to be synchronized
	private boolean hasGranted = false;
	private CSRequest grantedRequest = null;

	public QuorumRequestHandler(Node quorumNode, Client client) {
		super();
		this.quorumNode = quorumNode;
		this.queue = new PriorityQueue<CSRequest>(requestComparator);
		this.client = client;
	}

	public void handleRequest(CSRequest request) {
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

	public void handleYieldMessage(Node sourceNode) {
		logger.info("Received yield message from nodeId:{} in quorum nodeId:{}", sourceNode.getNodeId(),
				quorumNode.getNodeId());

		// Add the request back to the queue, since it has not been allowed to
		// enter CS
		queue.add(grantedRequest);

		if (grantedRequest.getNode() != sourceNode) {
			logger.error(
					"Something is wrong in quorum nodeId:{}. Received an yield message from nodeId:{} but the request was granted to nodeId:{}",
					quorumNode.getNodeId(), sourceNode.getNodeId(), grantedRequest.getNode().getNodeId());
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

	public void handleReleaseMessage(Node sourceNode, Message msg) {
		logger.info("Received release message from nodeId:{} in quorum nodeId:{}", sourceNode.getNodeId(),
				quorumNode.getNodeId());
		CSRequest request = queue.remove();

		if (grantedRequest.getNode() != sourceNode) {
			logger.error(
					"The nodeId:{} from which release message is obtained is not same as the nodeId:{} to which request was granted in the quorum nodeId:{}",
					sourceNode.getNodeId(), request.getNode().getNodeId());
		}

		if (!queue.isEmpty()) {
			// Send grant message to the next request
			request = queue.remove();
			sendGrantMessage(request);
		} else {
			logger.debug("No other request to be satisfied from quorum nodeId:{}", quorumNode.getNodeId());
		}

	}

	private void sendInquireMessage(CSRequest request, CSRequest previousReq) {
		Message msg = new Message(quorumNode.getNodeId(), previousReq.getNode().getNodeId(), MessageType.INQUIRE,
				previousReq.getNode().getPort());
		logger.info(
				"Sending inquire message to nodeId:{} from quorum nodeId:{} as request with TS:{} from nodeId:{} has to be serviced",
				previousReq.getNode().getNodeId(), quorumNode.getNodeId(), request.getTimestamp(),
				request.getNode().getNodeId());
		client.sendMsg(msg);
	}

	private Message sendGrantMessage(CSRequest request) {
		Message msg = new Message(quorumNode.getNodeId(), request.getNode().getNodeId(), MessageType.GRANT,
				request.getNode().getPort());
		logger.info("Sending grant message to the requesting nodeId:{} from the quorum nodeId:{} .Request TS:{}",
				request.getNode().getNodeId(), quorumNode.getNodeId(), request.getTimestamp());
		return msg;
	}

	private void sendFailedMessage(CSRequest request) {
		Message msg = new Message(quorumNode.getNodeId(), request.getNode().getNodeId(), MessageType.FAILED,
				request.getNode().getPort());
		logger.info("Sending failed message to the requesting nodeId:{} from quorum nodeId:{}",
				request.getNode().getNodeId(), quorumNode.getNodeId());
		client.sendMsg(msg);
	}

}
