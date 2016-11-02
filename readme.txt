1) Use the launcher script to start the program.
	./launcher-java.sh
	
	Optionally, the console log messages can be directed to a log file and run in the background.
	./launcher-java.sh > output.log &
	
2) Use the cleanup script to kill the running process, if needed.
	./cleanup.sh
	
3) Execute the following command
	grep "Critical Section:" output.log > result

4) Use the Python script to detect CS overlap by running the following command
	python TestCSOverlap result