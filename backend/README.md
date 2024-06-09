## Building the project
First, you need to install Java 17. 

### Apache Druid

In order to utilize all the functionalities of the backend you need to install [Apache Druid Database](https://druid.apache.org/).

We advise to start first the database and after that the other components of the system.

You can install it by [downloading](https://www.apache.org/dyn/closer.cgi?path=/druid/29.0.1/apache-druid-29.0.1-bin.tar.gz) it from the official website.

The database is designed to run on Unix-like OS (such as, Linux or Mac OS X). Even though Windows is not supported, it also works on WSL.

After installing Druid, in your terminal, extract the file and change directories to the distribution directory:

```bash
tar -xzf apache-druid-29.0.1-bin.tar.gz
cd apache-druid-29.0.1
```

One can start Druid by running the following command in the terminal: 
```bash
./bin/start-druid 
```

In WSL, when trying to run Druid you can get a ``CANNOT CREATE FIFO`` error. This is due to the fact that FIFO file can't be created over mounted drive
(`/mnt/c`, for example). An easy solution to this is copying the entire Druid installation folder to any internal folder (`/usr/share`, for example).

In order to further explore Druid you can access the application web console available at [http://localhost:8888](http://localhost:8888).

The configuration file for the database is located in the folder `backend/src/main/resources/ship-scores-kafka-supervisor.json`.

When located in the root file of the project you can ingest data to Druid by running the following command in terminal:

```bash
curl -X POST -H 'Content-Type: application/json' -d @backend/src/main/resources/ship-scores-kafka-supervisor.json http://localhost:8081/druid/indexer/v1/supervisor
```

This will create a configuration where ship data is retrieved from a Kafka topic called `ship-details` located at `localhost:9092`. This configuration will create a so-called supervisor.

When successful, the message `{"id" : "ship-details"}` will be printed in the terminal. In addition, there will be a supervisor named `ship-details` added to the [web console](http://localhost:8888/unified-console.html#supervisors).

After creating the supervisor, you can proceed to run the other parts of the systems. Be sure that Druid doesn't have any open past connections with the backend before starting it. You can check it by checking the `Tasks` tab in the [web console](http://localhost:8888/).

#### Resetting Druid

After the termination of the backend, one has to kill the connection between the database and the backend process. This can be done as mentioned above, in the web console by checking out the `Tasks` tab.

After finishing the execution of the system, one might want to delete the configuration file from the database.

In order to see the id of the active supervisors you can run 
```bash
curl "http://localhost:8888/druid/indexer/v1/supervisor"
```
After identifying the id of the supervisor that you want to terminate, you can run the following command:
```bash
curl --request POST "http://localhost:8888/druid/indexer/v1/supervisor/id/terminate"
```
Where `id` is the identifier of the supervisor that you want to terminate. Note that the terminated supervisors still exist in the metadata store and their history can be retrieved.
The data is persistently stored on disk in the structure of so-called segments. You can delete them through the [web console](http://localhost:8888/unified-console.html#segments).

You can find a more extensive list of supervisors' API at on the official [website](https://druid.apache.org/docs/latest/api-reference/supervisor-api/).

Note: It is enough to run this configuration command only once. Even after closing the database and opening it again, it will continue to ingest data from the same topic.

Also, the consumed data is deeply stored(stored on the disk) every hour with the current configuration, so if a crash happens, at most one hour of ship data will be lost from the database.

It is worth noting that Druid reads the data from the topic by considering its offset. In other words, if the topic is deleted and the details are re-written from the offset `0`, Druid will simply overwrite the processed ship details.

### Kafka

We recommend starting Kafka after starting the Apache Druid database.

First, you need to install Kafka. We use `kafka_2.13-3.7.0.tgz` from the [official website](https://kafka.apache.org/downloads).
You can download the exact Kafka configuration here: [kafka_2.13-3.7.0.tgz](https://downloads.apache.org/kafka/3.7.0/kafka_2.13-3.7.0.tgz).
Afterwards, it needs to be extracted.

If you use Windows, then WSL will have to be used to run Kafka. There are instructions for this online, but it is be as simple as just running all of this on WSL.

Afterwards, start 3 bash terminals. Locate to the kafka folder in each of the terminals.

Note: if you have managed to start the Druid database, you might be able to omit the first step.

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
bin/kafka-topics.sh --create --topic ships-raw-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic notifications --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic ships-history --bootstrap-server localhost:9092
```

If needed to remove the topics, run the following command:
```bash
bin/kafka-topics.sh --delete --topic ships-raw-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --delete --topic notifications --bootstrap-server localhost:9092
bin/kafka-topics.sh --delete --topic ships-history --bootstrap-server localhost:9092
```

Additionally, the following JVM argument needs to be added (Edit configurations -> Modify options -> (Java) Add VM options):
```
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.base/java.time=ALL-UNNAMED
--add-opens=java.base/java.lang=ALL-UNNAMED
```
## Running the project
In order to run the back-end of the project, a few steps will have to be made.

Make sure that Kafka and Zookeeper are running as described above. Leave these two terminals running for as long as you want to run the back-end.

### Start the pipeline and web server
Run the main project in IntelliJ (or just Gradle). This will start the pipeline and start listening for messages in the needed Kafka topics.
```bash
./gradlew bootRun
```

### Handling restarts of application
In between restarts of application, the Kafka Store storing the scores and positions should be cleaned up.
Do that by running the following command on yet another terminal window (while Kafka and Zookeeper are running):
```bash
bin/kafka-streams-application-reset.sh --application-id anomaly-detection-pipeline
```
If this command is not run in between restarts, the application will still work and that hashmap will be updated,
but for not-yet-seen values it will contain previously calculated data.

For full reset of the back-end state you can run the following commands (make sure the application is not runnning):
```bash
bin/kafka-streams-application-reset.sh --application-id anomaly-detection-pipeline --force
bin/kafka-topics.sh --delete --topic ships-raw-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --delete --topic notifications --bootstrap-server localhost:9092
bin/kafka-topics.sh --delete --topic ships-history --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic ships-raw-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic notifications --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic ships-history --bootstrap-server localhost:9092
```
In addition, you have to reset the database by following the steps mentioned above in the `Resetting Druid` section.

Sometimes Kafka might not start if the logs of Zookeeper and the Kafka server are not cleared. Assuming that `/tmp/kafka-logs` and `/tmp/zoekeeper` are the locations of the logs, run the following commands to delete them:
```bash
rm -rf /tmp/kafka-logs /tmp/zookeeper
```