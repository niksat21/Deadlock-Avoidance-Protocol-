package com.aos.lab2;

import java.util.*;

public class Config {

    private int noOfNodes;
    private int csExecTime;
    private int waitTime;
    private int noOfAttempts;
    private List<Node> nodes = new LinkedList<Node>();
    private Map<Integer, List<Integer>> nodeIdVsQuorum = new HashMap<Integer, List<Integer>>();
    private Map<Integer, Node> nodeIdVsNodes = new HashMap<Integer, Node>();

    public Config(int noOfNodes,int csExecTime,int waitTime,int noOfAttempts, List<Node> nodes, Map<Integer, List<Integer>> nodeIdVsPath) {
        super();
        this.noOfNodes = noOfNodes;
        this.csExecTime=csExecTime;
        this.waitTime=waitTime;
        this.noOfAttempts=noOfAttempts;
        this.nodes = nodes;
        this.nodeIdVsQuorum = nodeIdVsPath;
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

    public int getCsExecTime() {
        return csExecTime;
    }
    public int getWaitTime() {
        return waitTime;
    }
    public int getNoOfAttempts() {
        return noOfAttempts;
    }



    public List<Node> getNodes() {
        return nodes;
    }

    public List<Integer> getNodePathById(Integer id) {
        return nodeIdVsQuorum.get(id);
    }

    public Node getNodeById(Integer id) {
        return nodeIdVsNodes.get(id);
    }

    public Boolean checkIntersection(){
        int counter=0;
        for(Integer i : nodeIdVsQuorum.keySet()){
            List<Integer> first = new ArrayList<>();
            first = nodeIdVsQuorum.get(i);
            int tempc = 1;
            for(Integer j : nodeIdVsQuorum.keySet()){
                List<Integer> temp = new ArrayList<>();
                if(i!=j){
                    for(int m=0;m<nodeIdVsQuorum.get(j).size();m++)
                        temp.add(nodeIdVsQuorum.get(j).get(m));
                    temp.retainAll(first);
                    if(temp.size()>0)
                        tempc++;
                }
            }
            if(tempc==noOfNodes){
                counter++;
            }
        }
        if(counter==noOfNodes)
            return true;
        else
            return false;
    }

}