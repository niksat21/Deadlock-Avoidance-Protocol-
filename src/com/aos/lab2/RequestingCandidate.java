package com.aos.lab2;

public class RequestingCandidate{

    private Config config;
    private Integer nodeId;
    private Client client;
	
    public RequestingCandidate(Config config,Integer nodeId,Client client){
        this.config=config;
        this.nodeId=nodeId;
        this.client = client;
    }

    public void requestCS() throws InterruptedException{
        String version = config.getVersion();
        int count=0;
        int noOfRequests = config.getNoOfAttempts();
        Node node = config.getNodeById(nodeId);
        //sleep for some random time before making request for CS
        Thread.sleep(getExpoRandom(config.getWaitTime()));
        if(version.equals("preemptive")){
            while(count<=noOfRequests){
                PreemptiveCSHandler pcsh = new PreemptiveCSHandler(config,node,client,config.getNodeQuorumById(nodeId));
                pcsh.csEnter(System.currentTimeMillis());
                //sleep till CS is executed
                Thread.sleep(getExpoRandom(config.getCsExecTime()));
                pcsh.csLeave();
                count++;
            }
        }
        else if(version.equals("holdwait")){
            while(count<=noOfRequests){
                HoldAndWaitCSHandler hwcsh = new HoldAndWaitCSHandler(config,node,client,config.getNodeQuorumById(nodeId));
                hwcsh.csEnter(System.currentTimeMillis());
                //sleep till CS is executed
                Thread.sleep(getExpoRandom(config.getCsExecTime()));
                hwcsh.csLeave();
                count++;
            }
        }
        
    }
    
    private static int getExpoRandom(int mean){

        double temp = Math.random();
        double exp = -(Math.log(temp)*mean);

        return (int)exp;

	}
    
    
}