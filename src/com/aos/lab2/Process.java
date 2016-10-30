package com.aos.lab2;

import java.net.InetAddress;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Process {

	private static Logger logger = LogManager.getLogger(Process.class);
	private static Random rand = new Random();
	private static int labelValue = rand.nextInt(9) + 1;
	private static Integer nodeId;
	// private String version = "preemptive";

	public Process() {

	}

	public static void main(String[] args) {
		try {
			ConfigParser parser = new ConfigParser();
			Config config = parser.getConfig();

			if (args[0].equals("holdandwait")) {
				config.setVersion(DeadlockResolverType.HOLD_AND_WAIT);
			} else if (args[0].equals("preemptive")) {
				config.setVersion(DeadlockResolverType.PREEMPTIVE);
			} else {
				logger.error("Unsupported version: {}", args[0]);
			}

			String hostname = InetAddress.getLocalHost().getHostName();
			nodeId = Integer.valueOf(System.getProperty("nodeId"));
			logger.info("Hostname:{} NodeId:{} Label value:{}", hostname, nodeId, labelValue);
			Node node = config.getNodeById(nodeId);

			IQuorumRequestHandler quroumRequestHandler = null;
			if (config.getVersion().equals(DeadlockResolverType.HOLD_AND_WAIT)) {
				quroumRequestHandler = new HoldAndWaitQuorumRequestHandler(node, config);
			} else if (config.getVersion().equals(DeadlockResolverType.PREEMPTIVE)) {
				quroumRequestHandler = new PreemptiveQuorumRequestHandler(node, config);
			} else {
				logger.error("Unsupported config version: {}", config.getVersion().toString());
			}

			Server server = new Server(nodeId, labelValue, node.getPort(), config, quroumRequestHandler);
			Client client = new Client(hostname, labelValue, config, nodeId);
			server.setClientHandler(client);

			Thread clientThread = new Thread(client, "client-thread");
			Thread serverThread = new Thread(server, "server-thread");

			clientThread.start();
			serverThread.start();

			// while (true) {
			// if (ServerWorker.isCompleted()) {
			// serverThread.interrupt();
			// Thread.sleep(2000);
			// writeOutputToFile(hostname);
			// System.exit(0);
			// break;
			// }
			// Thread.sleep(3000);
			// }
			//
		} catch (Exception e) {
			logger.error("Exception in Process", e);
		}

	}

	// private static void writeOutputToFile(String hostname) throws IOException
	// {
	// BufferedWriter writer = new BufferedWriter(new FileWriter(hostname + "_"
	// + nodeId + ".txt"));
	// writer.write("Label value: " + labelValue);
	// writer.write("\nOutput value: " + ServerWorker.getResult());
	// writer.close();
	// }

	private static Integer getNodeId(String hostname, Config config) {
		for (Node node : config.getNodes()) {
			if (node.getHostname().equals(hostname))
				return node.getNodeId();
		}
		logger.error("Unable to find nodeId for hostname: {} in the list", hostname);
		return null;
	}

}
