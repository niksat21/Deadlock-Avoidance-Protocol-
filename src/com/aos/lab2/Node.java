package com.aos.lab2;

public class Node {

	private int nodeId;
	private String hostname;
	private Integer port;

	public Node(int nodeId, String hostname, int port) {
		super();
		this.nodeId = nodeId;
		this.hostname = hostname;
		this.port = port;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

}
