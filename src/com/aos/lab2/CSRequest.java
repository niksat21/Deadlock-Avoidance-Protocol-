package com.aos.lab2;

public class CSRequest implements Comparable<CSRequest> {

	private Integer nodeId;
	private Long timestamp;

	public CSRequest(Integer node, Long timestamp) {
		super();
		this.nodeId = node;
		this.timestamp = timestamp;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	@Override
	public int compareTo(CSRequest o) {
		if (o.getTimestamp() < this.timestamp)
			return -1;
		else if (o.getTimestamp() > this.timestamp)
			return 1;
		else {
			if (o.getNodeId() < this.nodeId)
				return -1;
			else
				return 1;
		}
	}

}
