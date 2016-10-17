package com.aos.lab2;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

public class ServerWorker implements Runnable {

	private volatile static Set<Integer> completedSet = new HashSet<Integer>();
	private volatile static Boolean isCompleted = false;
	private volatile static int result = -1;

	private Logger logger = LogManager.getLogger(ServerWorker.class);
	private Socket socket;
	private Integer nodeId;
	private Client client;
	private Integer labelValue;
	private Config config;

	public ServerWorker(Integer nodeId, Socket socket, Client client, Integer labelValue, Config config) {
		this.socket = socket;
		this.nodeId = nodeId;
		this.client = client;
		this.labelValue = labelValue;
		this.config = config;
	}

	@Override
	public void run() {
		try {
			// Sleep for sometime so that the other nodes come up.
			Thread.sleep(8000);
			DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			while (true && !isCompleted) {
				String json = reader.readLine();
				Gson gson = new Gson();
				logger.debug("Received msg:{} ", json);
				if (json != null) {
					String parsedJson = json.substring(2);
					logger.debug("Sanitized msg:{} ", parsedJson);
					Message msg = gson.fromJson(parsedJson, Message.class);
					if (msg.getMsgType().equals(MessageType.DATA)) {
						if (msg.getPath().size() == 0) {
							logger.info("Output value: {} from node:{} ", msg.getValue(), nodeId);
							result = msg.getValue();
							synchronized (isCompleted) {
								if (!isCompleted())
									client.broadcastCompletionMsg();
							}
						} else {
							msg.setSource(msg.getDestination());
							msg.setDestination(msg.getPath().remove(0));
							msg.setValue(msg.getValue() + labelValue);
							client.sendMsg(msg);
						}
					} else if (msg.getMsgType().equals(MessageType.COMPLETED)) {
						completedSet.add(msg.getSource());
						logger.debug("NodeId:{} Completed Set size:{}. CompletedSet:{}", nodeId, completedSet.size(),
								completedSet);

						// Close socket if it has received the broadcast message
						// from other nodes.
						if (checkCompletion())
							return;
					} else {
						logger.error("Unknown message type: {} in the message:{}", msg.getMsgType(), json);
					}
				} else if (checkCompletion()) {
					return;
				} else {

					logger.debug("Sleeping for 3 seconds as there is nothing to read in socket from host:{}",
							socket.getRemoteSocketAddress());
					Thread.sleep(3000);
				}
			}
		} catch (Exception e) {
			logger.error("Exception in Server Worker thread", e);
		}
	}

	private boolean checkCompletion() throws IOException {
		synchronized (isCompleted) {
			if (completedSet.size() == config.getNoOfNodes() - 1 && result != -1) {
				logger.debug("Closing socket between local host:{} and remote host:{}",
						socket.getLocalAddress().getHostName(), socket.getRemoteSocketAddress());
				socket.close();
				client.closeSockets();
				isCompleted = true;
				return true;
			}
			return false;
		}
	}

	public static boolean isCompleted() {
		return isCompleted;
	}

	public static int getResult() {
		return result;
	}

	public static void main(String[] args) {
		String json = "O{\"source\":1,\"destination\":0,\"path\":[1],\"value\":0,\"msgType\":\"DATA\",\"port\":1252}";
		json = json.substring(1);
		System.out.println(json);
	}
}
