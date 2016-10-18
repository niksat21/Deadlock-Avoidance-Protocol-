package com.aos.lab2;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreemptiveCSHandler implements ICriticalSectionHandler {

	private static final Logger logger = LogManager.getLogger(PreemptiveCSHandler.class);
	
	private Config config;
	
	public PreemptiveCSHandler(Config config) {
		super();
		this.config = config;
	}

	@Override
	public void csEnter() {
		// TODO Auto-generated method stub

	}

	@Override
	public void csLeave() {
		// TODO Auto-generated method stub

	}

}
