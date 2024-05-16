## Building the project
First, you need to install Java 17. 

Then, you need to install Kafka. We use `kafka_2.13-3.7.0.tgz` from the [official website](https://kafka.apache.org/downloads).
You can download the exact Kafka configuration here: [kafka_2.13-3.7.0.tgz](https://downloads.apache.org/kafka/3.7.0/kafka_2.13-3.7.0.tgz).
Afterwards, it needs to be extracted.

If you use Windows, then WSL will have to be used to run Kafka. There are instructions for this online, but it should be as simple as just running all of this on WSL.

Inside of the Kafka folder, we modify the properties in the `config` folder as follows:
- [At the moment we use fully default settings].

Afterwards, start 3 bash terminals. Locate to the kafka folder in each of the terminals.
In the first one, run the following command to start the Zookeeper server:
```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
```
In the second one, run the following command to start the Kafka server:
```bash
bin/kafka-server-start.sh config/server.properties
```

Finally, create the topics required for the project. In the third terminal, run the following commands:
```bash
bin/kafka-topics.sh --create --topic ships-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic ships-scores --bootstrap-server localhost:9092
```

If needed to remove the topics, run the following command:
```bash
bin/kafka-topics.sh --delete --topic ships-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --delete --topic ships-scores --bootstrap-server localhost:9092
```

Additionally, the following JVM argument needs to be added (Edit configurations -> Modify options -> (Java) Add VM options):
```
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.base/java.time=ALL-UNNAMED
```
## Running the project
In order to run the back-end of the project, multiple steps will have to be made.

### Start the Kafka server
Open 2 terminals and locate to the kafka folder in each of them. In the first one, run the following command to start the Zookeeper server:
```bash
bin/zookeeper-server-start.sh config/zookeeper.properties

Leave these terminals running for as long as you want to run the back-end.

In between restarts of application, the hashmap storing the scores and positions should be cleaned up.
Do that by running the following command on yet another terminal window (while Kafka and Zookeeper are running):
```bash
bin/kafka-streams-application-reset.sh --application-id anomaly-detection-pipeline
```
If this command is not run in between restarts, the application will still work and that hashmap will be updated,
but for not-yet-seen values it will contain previously calculated data.

For full reset of the back-end state you can run the following commands (make sure the application is not runnning):
```bash
bin/kafka-streams-application-reset.sh --application-id anomaly-detection-pipeline
bin/kafka-topics.sh --delete --topic ships-raw-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --delete --topic ships-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --delete --topic ships-scores --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic ships-raw-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic ships-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic ships-scores --bootstrap-server localhost:9092
```

Sometimes Kafka might not start if the logs of Zookeeper and the Kafka server are not cleared. Assuming that `/tmp/kafka-logs` and `/tmp/zoekeeper` are the locations of the logs, run the following commands to delete them:
```bash
rm -rf /tmp/kafka-logs /tmp/zookeeper
```


### Start the pipeline and web server
Run the main project in IntelliJ (or just Gradle). This will start the pipeline and start listening for messages in the needed Kafka topics.

