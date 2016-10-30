package com.aos.lab2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

public class Client {

	private Map<Integer, Socket> nodeVsSocket = new HashMap<Integer, Socket>();
	private Logger logger = LogManager.getLogger(Client.class);
	private String nodeHostname = null;
	private int labelValue;
	private Config config;
	private Integer nodeId;

	public Client(String nodeHostname, int labelValue, Config config, Integer nodeId) {
		this.nodeHostname = nodeHostname;
		this.labelValue = labelValue;
		this.config = config;
		this.nodeId = nodeId;
		logger.debug("Sleeping for 8 seconds until other nodes come up");
		Thread.sleep(8000);
		createSockets(config.getNodes());
		initiateMsg();
	}

	// Open connections with every other node
	private void createSockets(List<Node> nodes) {
		for (Node node : nodes) {
			try {
				if (node.getNodeId().equals(nodeId))
					continue;
				logger.debug("Trying to create connection with host:{} port:{}", node.getHostname(), node.getPort());
				Socket socket = new Socket(node.getHostname(), node.getPort());
				nodeVsSocket.put(node.getNodeId(), socket);
				logger.debug("Successfully created socket connection to host:{} from:{} ", node.getHostname(),
						nodeHostname);
			} catch (UnknownHostException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
		}
		logger.debug("NodeVsSocketMap:{}", nodeVsSocket);
	}

	public void closeSockets() {
		try {
			for (Entry<Integer, Socket> entry : nodeVsSocket.entrySet()) {
				Socket socket = entry.getValue();
				logger.debug("Closing socket between local host:{} and remote host:{}", socket.getLocalSocketAddress(),
						socket.getRemoteSocketAddress());
				socket.close();
			}
		} catch (IOException e) {
			logger.error("Problem with closing the socket.", e);
		}
	}

	@Override
//	public void run() {
//		try {
//			// Sleep for sometime so that the other nodes come up.
//			logger.debug("Sleeping for 8 seconds until other nodes come up");
//			Thread.sleep(8000);
//			createSockets(config.getNodes());
//			initiateMsg();
//
//		} catch (Exception e) {
//			logger.error("Problem in client thread.", e);
//		}
//	}

	public void sendMsg(Message msg) {
		Socket socket = nodeVsSocket.get(msg.getDestination());
		Gson gson = new Gson();
		String json = gson.toJson(msg);
		while (true) {
			try {
				if (socket != null) {
					logger.debug("Trying to send msg:{} from nodeId:{}", json, nodeId);
					DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
					outputStream.writeUTF(json + "\n");
					logger.debug("Successfully placed msg: {} in the stream to host:{}", json,
							socket.getRemoteSocketAddress());
					break;
				} else {
					logger.error(
							"Unable to find socket for msg:{} destination nodeId:{}. SocketMap: {} Retry after 5 seconds",
							json, msg.getDestination(), nodeVsSocket);
					Thread.sleep(5000);
				}
			} catch (Exception e) {
				logger.error("Exception in client thread", e);
			}
		}
	}

	private void initiateMsg() {
		for (Node node : config.getNodes()) {
			if (node.getNodeId().equals(nodeId)) {
				List<Integer> path = config.getNodePathById(node.getNodeId());
				if (path != null && !path.isEmpty()) {
					Node dest = config.getNodeById(path.remove(0));
					Message msg = new Message(node.getNodeId(), dest.getNodeId(), dest.getPort(), path, labelValue,
							MessageType.DATA);
					sendMsg(msg);
				} else {
					logger.warn("No message to initiate from nodeId:" + node.getNodeId());
				}
				break;
			}
		}
	}

	public void broadcastCompletionMsg() {
		logger.debug("Broadcasting completion message from host:{}", nodeHostname);
		for (Node node : config.getNodes()) {
			// Ignore sending the completion message to itself
			if (node.getNodeId().equals(nodeId))
				continue;
			Message msg = new Message(nodeId, node.getNodeId(), node.getPort(), null, 0, MessageType.COMPLETED);
			sendMsg(msg);
		}

	}

}
