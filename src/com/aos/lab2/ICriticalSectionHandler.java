package com.aos.lab2;

public interface ICriticalSectionHandler {

	public void csEnter(Long timestamp) throws InterruptedException;

	public void csLeave();

}
