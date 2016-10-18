package com.aos.lab2;

public class CSRequest implements Comparable<CSRequest> {

	private Node node;
	private long timestamp;

	public CSRequest(Node node, long timestamp) {
		super();
		this.node = node;
		this.timestamp = timestamp;
	}

	public Node getNode() {
		return node;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int compareTo(CSRequest o) {
		if (o.getTimestamp() < this.timestamp)
			return -1;
		else if (o.getTimestamp() > this.timestamp)
			return 1;
		else {
			if (o.getNode().getNodeId() < this.node.getNodeId())
				return -1;
			else
				return 1;
		}
	}

}
