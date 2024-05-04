# Simulator documentation
## About
Simulator is an application for simulating the stream of AIS signals that should be sent by actual ships. This application turns a list of hardcoded AIS signals to an actual stream, where each signal is appended to a certain Kafka topic at its corresponding time. In our implementation of simulator, it is possible to:
1. Add any dataset of any format for stream signals, as long as a needed parser for it is implemented.
2. Choose whether to sort the signals according to their timestamp.
3. Choose a starting timestamp for the signals.
4. Choose an ending timestamp for the signals.
5. Modify the streaming speed of the signals. 
6. Choose an arbitrary Kafka topic.
7. Choose an arbitrary server.

## Building the project

In order to build the project, the following steps should be followed:
1. Make sure you have Java installed. That could be done by running `java -version` in terminal. If java is not installed, run 
```
sudo apt update
sudo apt install default-jdk
 ``` 
For this project's environment, I used Java 17.
2. Make sure that Kafka is installed. If not, that that could be done by following the procedure described in backend project readme.md file. 

## Starting the project

In order to start the simulator, the following steps should be taken:
1. A wanted dataset file should be added to `streaming_data` directory. For now, the default dataset that we are using is downloaded from `https://www.marinetraffic.com/research/dataset/marinetraffic-automatic-identification-system-ais/`.
2. The following parameters should be instantiated in the `main()` method:
   1. Name of the Kafka topic
   2. Server name
   3. Parser type
   4. Name of the dataset
   5. Start time of the stream
   6. End time of the stream
   7. Stream speed (optional)
3. Kafka server should be started and a wanted topic should be created (and possibly a new consumer window added). Once again, the documentation of how that could be done is presented in backend project readme.md file.
4. Then, the simulator can be started by running the `main()` method. It will start appending messages to the specified topic. Please make sure that the name of the topic in the simulator matches the one created in Kafka.
5. When the stream stops, Kafka could be stopped by running `kafka-server-stop`

## Additional information
1. If a dataset of a new format is added for which a parser has not been implemented, a wanted parser could be implemented by adding a new parser class in 'parsers' directory.
2. The AIS signals are sent as AIS class objects converted to a json string. This is a matter of convention.

   

