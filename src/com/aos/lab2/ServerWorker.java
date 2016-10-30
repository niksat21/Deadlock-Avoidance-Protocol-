package com.aos.lab2;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.aos.lab2.Server.AssociationHandler;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class ServerWorker implements Runnable {

	private volatile static Set<Integer> completedSet = new HashSet<Integer>();
	private volatile static Boolean isCompleted = false;
	private volatile static int result = -1;

	private Logger logger = LogManager.getLogger(ServerWorker.class);
	private SctpChannel sc;
	private Integer nodeId;
	private Client client;
	private Integer labelValue;
	private Config config;
	private AssociationHandler assocHandler;
	private IQuorumRequestHandler quorumRequestHandler;

	public ServerWorker(Integer nodeId, SctpChannel sc, Client client, Integer labelValue, Config config,
			AssociationHandler assocHandler, IQuorumRequestHandler quorumRequestHandler) {
		this.sc = sc;
		this.nodeId = nodeId;
		this.client = client;
		this.labelValue = labelValue;
		this.config = config;
		this.assocHandler = assocHandler;
		this.quorumRequestHandler = quorumRequestHandler;
	}

	@Override
	public void run() {
		try {
			// Sleep for sometime so that the other nodes come up.
			Thread.sleep(8000);
			// DataInputStream inputStream = new
			// DataInputStream(socket.getInputStream());
			// DataOutputStream outputStream = new
			// DataOutputStream(socket.getOutputStream());
			// BufferedReader reader = new BufferedReader(new
			// InputStreamReader(inputStream, "UTF-8"));

			while (true /* && !isCompleted */) {
				ByteBuffer buf = ByteBuffer.allocateDirect(500000);
				MessageInfo messageInfo = sc.receive(buf, System.out, assocHandler);
				buf.flip();
				byte[] data = new byte[buf.remaining()];
				buf.get(data, 0, data.length);
				ByteArrayInputStream bis = new ByteArrayInputStream(data);
				ObjectInputStream ois = new ObjectInputStream(bis);
				Message msg = (Message) ois.readObject();

				// clearing buffer
				buf.clear();

				if (msg.getMsgType().equals(MessageType.REQUEST)) {
					quorumRequestHandler.handleRequestMessage(new CSRequest(msg.getSource(), msg.getRequestTS()));
				} else if (msg.getMsgType().equals(MessageType.RELEASE)) {
					quorumRequestHandler.handleReleaseMessage(msg.getSource());
				} else if (msg.getMsgType().equals(MessageType.YIELD)) {
					quorumRequestHandler.handleReleaseMessage(msg.getSource());
				} else {
					logger.error("Unsupported message type : {} by the quorum handler", msg.getMsgType().toString());
				}

				// String json = reader.readLine();
				// Gson gson = new Gson();
				// logger.debug("Received msg:{} ", json);
				// if (json != null) {
				// String parsedJson = json.substring(2);
				// logger.debug("Sanitized msg:{} ", parsedJson);
				// Message msg = gson.fromJson(parsedJson, Message.class);
				// if (msg.getMsgType().equals(MessageType.DATA)) {
				// if (msg.getPath().size() == 0) {
				// logger.info("Output value: {} from node:{} ", msg.getValue(),
				// nodeId);
				// result = msg.getValue();
				// synchronized (isCompleted) {
				// if (!isCompleted())
				// client.broadcastCompletionMsg();
				// }
				// } else {
				// msg.setSource(msg.getDestination());
				// msg.setDestination(msg.getPath().remove(0));
				// msg.setValue(msg.getValue() + labelValue);
				// client.sendMsg(msg);
				// }
				// } else if (msg.getMsgType().equals(MessageType.COMPLETED)) {
				// completedSet.add(msg.getSource());
				// logger.debug("NodeId:{} Completed Set size:{}.
				// CompletedSet:{}", nodeId, completedSet.size(),
				// completedSet);
				//
				// // Close socket if it has received the broadcast message
				// // from other nodes.
				// if (checkCompletion())
				// return;
				// } else {
				// logger.error("Unknown message type: {} in the message:{}",
				// msg.getMsgType(), json);
				// }
				// } else if (checkCompletion()) {
				// return;
				// } else {
				//
				// logger.debug("Sleeping for 3 seconds as there is nothing to
				// read in socket from host:{}",
				// socket.getRemoteSocketAddress());
				// Thread.sleep(3000);
				// }
			}
		} catch (Exception e) {
			logger.error("Exception in Server Worker thread", e);
			// try {
			// if(ois!=null)
			// ois.close();
			// if(bis!=null)
			// bis.close();
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
		}
	}

	// private boolean checkCompletion() throws IOException {
	// synchronized (isCompleted) {
	// if (completedSet.size() == config.getNoOfNodes() - 1 && result != -1) {
	// logger.debug("Closing socket between local host:{} and remote host:{}"/*,
	// socket.getLocalAddress().getHostName(),
	// socket.getRemoteSocketAddress()*/);
	// socket.close();
	// client.closeSockets();
	// isCompleted = true;
	// return true;
	// }
	// return false;
	// }
	// }

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
