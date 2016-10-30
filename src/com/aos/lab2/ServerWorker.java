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
			}
		} catch (Exception e) {
			logger.error("Exception in Server Worker thread", e);
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
