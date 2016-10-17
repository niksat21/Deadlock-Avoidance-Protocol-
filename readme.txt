1) Use the launcher script to start the program.
	./launcher-java.sh
	
	Optionally, the console log messages can be directed to a log file and run in the background.
	./launcher-java.sh > output.log &
	
2) Use the cleanup script to kill the running process, if needed.
	./cleanup.sh
	
3) The output file is named as <hostname>_<nodeId>.txt and is present in the home directory.