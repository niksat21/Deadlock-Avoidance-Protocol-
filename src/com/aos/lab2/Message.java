package com.aos.lab2;

import java.util.LinkedList;
import java.util.List;

public class Message {

	private Integer source;
	private Integer destination;
	private List<Integer> path = new LinkedList<Integer>();
	private Integer value;
	private MessageType msgType;
	private Integer port;

	public Message(Integer source, Integer destination, Integer port, List<Integer> path, Integer value,
			MessageType msgType) {
		this.source = source;
		this.destination = destination;
		this.path = path;
		this.value = value;
		this.msgType = msgType;
		this.port = port;
	}

	public Integer getSource() {
		return source;
	}

	public Integer getDestination() {
		return destination;
	}

	public List<Integer> getPath() {
		return path;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public MessageType getMsgType() {
		return msgType;
	}

	public void setSource(Integer source) {
		this.source = source;
	}

	public void setDestination(Integer destination) {
		this.destination = destination;
	}

	public void setPath(List<Integer> path) {
		this.path = path;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

}
