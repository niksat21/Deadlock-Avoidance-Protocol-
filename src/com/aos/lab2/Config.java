package com.aos.lab2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Config {

	private int noOfNodes;
	private List<Node> nodes = new LinkedList<Node>();
	private Map<Integer, List<Integer>> nodeIdVsPath = new HashMap<Integer, List<Integer>>();
	private Map<Integer, Node> nodeIdVsNodes = new HashMap<Integer, Node>();

	public Config(int noOfNodes, List<Node> nodes, Map<Integer, List<Integer>> nodeIdVsPath) {
		super();
		this.noOfNodes = noOfNodes;
		this.nodes = nodes;
		this.nodeIdVsPath = nodeIdVsPath;
		populateNodeIdVsNodesMap();
	}

	private void populateNodeIdVsNodesMap() {
		for (Node node : nodes) {
			nodeIdVsNodes.put(node.getNodeId(), node);
		}
	}

	public int getNoOfNodes() {
		return noOfNodes;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<Integer> getNodePathById(Integer id) {
		return nodeIdVsPath.get(id);
	}

	public Node getNodeById(Integer id) {
		return nodeIdVsNodes.get(id);
	}

}
