package com.aos.lab2;

public class RequestingCandidate{

    private config = null;
    public RequestingCandidate(Config config){
        this.config=config;
    }

    public void requestCS(){
        String version = config.getVersion();
        int count=0;
        int noOfRequests = config.getNoOfAttempts();
        //sleep for some random time before making request for CS
        Thread.sleep(getExpoRandom(config.getWaitTime()));
        if(version.equals("preemptive")){
            while(count<=noOfRequests){
                PreemptiveCSHandler pcsh = new PreemptiveCSHandler(config,nodeId,client,config.getNodeQuorumById(nodeId));
                pcsh.csEnter(System.currentTimeMillis());
                //sleep till CS is executed
                Thread.sleep(getExpoRandom(config.getCsExecTime()));
                pcsh.csLeave();
                count++;
            }
        }
        else if(version.equals("holdwait")){
            while(count<=noOfRequests){
                HoldAndWaitCSHandler hwcsh = new HoldAndWaitCSHandler(config,nodeId,client,config.getNodeQuorumById(nodeId));
                hwcsh.csEnter(System.currentTimeMillis());
                //sleep till CS is executed
                Thread.sleep(getExpoRandom(config.getCsExecTime()));
                hwcsh.csLeave();
                count++;
            }
        }
    }
}