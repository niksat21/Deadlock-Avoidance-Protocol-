package com.aos.lab2;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import com.sun.nio.sctp.ShutdownNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server implements Runnable {

	private Client client;
	private Logger logger = LogManager.getLogger(Server.class);
	private int port;
	private Integer nodeId;
	private Integer labelValue;
	private Config config;

	public Server(Integer nodeId, Integer labelValue, Integer port, Config config) {
		this.nodeId = nodeId;
		this.labelValue = labelValue;
		this.port = port;
		this.config = config;
	}

	@Override
	public void run() {
		try {
			listenForConnections();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void setClientHandler(Client client) {
		this.client = client;
	}

	private void listenForConnections() throws /*UnknownHostException, IOException*/Exception {
		//ServerSocket socket = new ServerSocket(port);
		SctpServerChannel ssc = SctpServerChannel.open();
		InetSocketAddress serverAddr = new InetSocketAddress(port);
		ssc.bind(serverAddr);
		AssociationHandler assocHandler = new AssociationHandler();
		try {
			while (true) {
				logger.debug("Listening for connection on hostname:{} port:{}"/*,
						socket.getInetAddress().getHostAddress(), socket.getLocalPort()*/);
				//Socket sock = socket.accept();
				SctpChannel sc = ssc.accept();
				ServerWorker worker = new ServerWorker(nodeId, sc, client, labelValue, config, assocHandler);
				logger.debug("Created server worker");
				Thread workerThread = new Thread(worker);
				logger.debug("Created server worker thread");
				workerThread.start();
			}
		} finally {
			ssc.close();
		}
	}
	
	static class AssociationHandler extends AbstractNotificationHandler<PrintStream>
	{
	   public HandlerResult handleNotification(AssociationChangeNotification not,
	                                           PrintStream stream) {
	       if (not.event().equals(AssociationChangeNotification.AssocChangeEvent.COMM_UP)) {
	           int outbound = not.association().maxOutboundStreams();
	           int inbound = not.association().maxInboundStreams();
	           //stream.printf("New association setup with %d outbound streams" +
	            //             ", and %d inbound streams.\n", outbound, inbound);
	       }

	       return HandlerResult.CONTINUE;
	   }

	   public HandlerResult handleNotification(ShutdownNotification not,
	                                           PrintStream stream) {
	       //stream.printf("The association has been shutdown.\n");
	       return HandlerResult.RETURN;
	   }
	}
	
}