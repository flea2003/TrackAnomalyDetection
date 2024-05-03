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
2. Make sure that Kafka is installed. If not, that that could be done by running the following command: 
```
brew install kafka
```
## Starting the project

In order to start the simulator, the following steps should be taken:
1. A wanted dataset file should be added, preferably, in `streaming_data` directory
2. The following parameters should be instantiated in the `main()` method:
   1. Name of the Kafka topic
   2. Server name
   3. Parser type
   4. Path to dataset
   5. Start time of the stream
   6. End time of the stream
   7. Stream speed (optional)
3. ZooKeeper server should be started. That could be done by running `zkServer start` or `zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties`
4. Kafka should be started. That could be done by running `kafka-server-start /usr/local/etc/kafka/server.properties`
5. A new Kafka topic should be added: `bin/kafka-topics.sh --create --topic test --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1`. Here the name of the topic is 'test', but that could be changed to any other string.
6. In new terminal window, a consumer should be added in order to see what messages are appended to the topic. That could be done by running `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning`
7. Then, the simulator can be started by running the `main()` method. It will start appending messages to the specified topic.
8. When the stream stops, Kafka could be stopped by running `kafka-server-stop`

## Additional information
1. If a dataset of a new format is added for which a parser has not been implemented, a wanted parser could be implemented by adding a new parser class in 'parsers' directory.
2. The AIS signals are sent as AIS class objects converted to a json string. This is a matter of convention.

   

