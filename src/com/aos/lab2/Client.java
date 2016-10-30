package com.aos.lab2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.text.SimpleDateFormat;
import java.nio.channels.spi.AbstractSelectableChannel;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.ShutdownNotification;
import com.sun.nio.sctp.HandlerResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

public class Client implements Runnable {

	private Map<Integer, SocketAddress> nodeVsSocket = new HashMap<Integer, SocketAddress>();
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

	}

	// Open connections with every other node
	private void createSockets(List<Node> nodes) {
		for (Node node : nodes) {
			try {
				if (node.getNodeId().equals(nodeId))
					continue;
				logger.debug("Trying to create connection with host:{} port:{}", node.getHostname(), node.getPort());
				//Socket socket = new Socket(node.getHostname(), node.getPort());
				SocketAddress socketAddress = new InetSocketAddress(node.getHostname(), node.getPort());
				nodeVsSocket.put(node.getNodeId(), socketAddress);
				logger.debug("Successfully created socket connection to host:{} from:{} ", node.getHostname(),
						nodeHostname);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		logger.debug("NodeVsSocketMap:{}", nodeVsSocket);
	}

	public void closeSockets() {
		try {
			for (Entry<Integer, SocketAddress> entry : nodeVsSocket.entrySet()) {
				SocketAddress socketAddress = entry.getValue();
				logger.debug("Closing socket between local host:{} and remote host:{}");
				//socketAddress.close();
			}
		} catch (Exception e) {
			logger.error("Problem with closing the socket.", e);
		}
	}

	@Override
	public void run() {
		try {
			// Sleep for sometime so that the other nodes come up.
			logger.debug("Sleeping for 8 seconds until other nodes come up");
			Thread.sleep(8000);
			createSockets(config.getNodes());
            if(config.getNodeQuorumById(nodeId).size()!=0){
                RequestingCandidate rc = new RequestingCandidate(config,nodeId,this);
                rc.requestCS();
            }
			//initiateMsg();

		} catch (Exception e) {
			logger.error("Problem in client thread.", e);
		}
	}

	public void sendMsg(Message msg) {
		//Socket socket = nodeVsSocket.get(msg.getDestination());
		SocketAddress socketAddress = nodeVsSocket.get(msg.getDestination());
		Gson gson = new Gson();
		String json = gson.toJson(msg);
		while (true) {
			try {
//				if (socket != null) {
//					logger.debug("Trying to send msg:{} from nodeId:{}", json, nodeId);
//					DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
//					outputStream.writeUTF(json + "\n");
//					logger.debug("Successfully placed msg: {} in the stream to host:{}", json,
//							socket.getRemoteSocketAddress());
//					break;
//				} else {
//					logger.error(
//							"Unable to find socket for msg:{} destination nodeId:{}. SocketMap: {} Retry after 5 seconds",
//							json, msg.getDestination(), nodeVsSocket);
//					Thread.sleep(5000);
//				}
//			} catch (Exception e) {
//				logger.error("Exception in client thread", e);
//			}
//		}
		SctpChannel sctpChannel = SctpChannel.open();
		sctpChannel.connect(socketAddress);
				
		MessageInfo messageInfo = MessageInfo.createOutgoing(socketAddress, 0);
		ByteBuffer buf = ByteBuffer.allocateDirect(500000);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(msg);
		oos.flush();
		buf.put(bos.toByteArray());
		buf.flip();
		sctpChannel.send(buf, messageInfo);
		
		bos.close();
		buf.clear();
		//return true;
	}catch(Exception e){
		logger.warn("Exception in Send()"+ e);
		//System.err.println(e);
		e.printStackTrace();
		//return false;
	}
	}
	}

//	private void initiateMsg() {
//		for (Node node : config.getNodes()) {
//			if (node.getNodeId().equals(nodeId)) {
//				List<Integer> path = config.getNodePathById(node.getNodeId());
//				if (path != null && !path.isEmpty()) {
//					Node dest = config.getNodeById(path.remove(0));
//					Message msg = new Message(node.getNodeId(), dest.getNodeId(), dest.getPort(), path, labelValue,
//							MessageType.DATA);
//					sendMsg(msg);
//				} else {
//					logger.warn("No message to initiate from nodeId:" + node.getNodeId());
//				}
//				break;
//			}
//		}
//	}

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
