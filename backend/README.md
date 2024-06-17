# Backend

Backend is the part of the application which implements the logic for consuming the Kafka topics, processing stream of AIS signals data, and the logic for web server as well. 

## Table of Contents
- [Running Backend](#running-backend)
  - [Distributed Flink Cluster (Optional)](#distributed-flink-cluster-optional)
- [Running Tests and Static Checks](#running-tests-and-static-checks)

## Running Backend

**Before starting the frontend, you need to set up and start the Druid and Kafka.** The instructions can be found in the [README.md in the folder above](../README.md).

Note: if you want to run the distributed Flink cluster, check the instructions [below](#distributed-flink-cluster-optional).

You need to build and then start the backend using the following commands:
```shell
cd codebase/backend
chmod +x ./gradlew # make the wrapper script executable
./gradlew clean # optional, if you want to clean the previous build
./gradlew build # build the project; also runs tests and static analysis
./gradlew bootRun # starts the web server which now can be
                  # reached at http://localhost:8180/
```

To check that the backend started, you can go to http://localhost:8180/ships/details.

If you want to start the application inside the IDE, you need to start [BackendApplication.java](src/main/java/sp/BackendApplication.java). However, note that additional JVM arguments are required 
(to edit them inside IntelliJ IDEA, go to `Edit configurations` -> `Modify options` -> `(Java) Add VM options`). The required arguments are:
```
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.base/java.time=ALL-UNNAMED
--add-opens=java.base/java.lang=ALL-UNNAMED
```

### Distributed Flink Cluster (Optional)
By default, the application runs a local Flink cluster. In particular, starting the application also starts a Flink cluster on the
same machine. However, in practice, using a distributed Flink cluster is recommended, since it allows for large scaling capabilities.

If you wish to use an external Flink cluster, once you have set it up, you will (or may) need to update the following parameters in this project:
- `flink.jobmanager.host` in `kafka-connection.properties` to the IP address of the external Flink cluster's job manager.
- `flink.jobmanager.port` in `kafka-connection.properties` to the port of the external Flink cluster's job manager (most likely 8084).
- `bootstrap.servers` in `kafka-connection.properties` to the IP address of the Kafka server - make sure this IP is accessible from all the Flink cluster
- `kafka.server.address` in `kafka-connection.properties` to the IP address of the Kafka server - make sure this IP is accessible from all the Flink cluster
- `kafka.server.port` in `kafka-connection.properties` to the port of the Kafka server (most likely 9092).
- In AnomalyDetectionPipeline class, change the injected flink environment qualifier from `localFlinkEnv` to `distributedFlinkEnv`. I.e., change the injected bean.

Additionally, before running the application, you need to run `./gradlew shadowJar`, to make sure that a Jar containing the dependencies
for the Flink job is created. Running this command is only necessary in the case of using an external Flink cluster.

Furthermore, you might have to change a file in Kafka: change `config/server.properties` file by adding the following 2 lines:
```
listeners=PLAINTEXT://0.0.0.0:9092
advertised.listeners=PLAINTEXT://<IP>:9092
```
Where <IP> is the IP of the computer where Kafka is running (in case you are using WSL - IP means the IP of the main computer,
not the IP of the WSL container).

For configuring the Flink cluster, you might want to edit Flink's `config.yaml` file. For us, the default configuration (with 
only changed ports and IP addresses) worked quite poorly, and the file had to be greatly modified. The configuration that was used for 
distributability testing can be found in the developer manual.


Additionally, if you are using WSL, and you wish to have other parts of the cluster running on different machines, you might
have to expose some WSL ports as the main machine's ports. To do that, use the following command, ran from Powershell with
Administrator privileges:

```
netsh interface portproxy add v4tov4 listenport=<port> listenaddress=0.0.0.0 connectport=<same port> connectaddress=<WSL IP>
```

Where `<port>` is the port you want to expose, and `<WSL IP>` is the IP address of the WSL machine. You can find the IP address of
WSL by running `ifconfig` in the WSL terminal.

If all of these steps are done correctly, then once the application is started, it will submit a Flink job to the Flink job manager,
and the job will run on the external cluster, as a separate entity from the application.

A detailed guide on how to set up external Flink cluster and reproduce our distributed setting can be found in the **developer manual**.


## Running Tests and Static Checks

To ensure code quality of the backend code, the following tools are used (also included in the GitLab pipeline):
- [JUnit](https://junit.org/junit5/) and [AssertJ](https://assertj.github.io/doc/) frameworks for unit testing.
- [JaCoCo (Java Code Coverage)](https://www.eclemma.org/jacoco/) for analyzing the coverage.
- [PIT Mutation Testing](https://pitest.org/) framework.
- [CheckStyle](https://checkstyle.sourceforge.io/) code style checker.
- [PMD](https://pmd.github.io/) static code analyzer.

You can run them using the following commands:
```shell
cd codebase/backend
chmod +x ./gradlew # make the wrapper script executable

./gradlew test jacocoTestReport # runs unit tests, the JaCoCo report is generated at
                                # codebase/backend/build/reports/jacoco/test/html/index.html

./gradlew pitest # runs mutation testing, the report is generated at
                 # codebase/backend/build/reports/pitest/index.html

./gradlew checkstyleMain # runs checkstyle, the report is generated at 
                         # codebase/backend/build/reports/checkstyle/main.html
                         
./gradlew pmdMain # runs PMD, the report is generated at
                  # codebase/backend/build/reports/pmd/main.html
```