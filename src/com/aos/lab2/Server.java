package com.aos.lab2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

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
		} catch (UnknownHostException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}

	}

	public void setClientHandler(Client client) {
		this.client = client;
	}

	private void listenForConnections() throws UnknownHostException, IOException {
		ServerSocket socket = new ServerSocket(port);
		try {
			while (true) {
				logger.debug("Listening for connection on hostname:{} port:{}",
						socket.getInetAddress().getHostAddress(), socket.getLocalPort());
				Socket sock = socket.accept();
				ServerWorker worker = new ServerWorker(nodeId, sock, client, labelValue, config);
				logger.debug("Created server worker");
				Thread workerThread = new Thread(worker);
				logger.debug("Created server worker thread");
				workerThread.start();
			}
		} finally {
			socket.close();
		}
	}

}
