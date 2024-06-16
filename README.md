# Anomaly Detection Pipeline

The source code for the project Anomaly Detection Pipeline developed by team 18A
for the course *Software Project* at TU Delft. It has been developed for the Dutch Ministry of
Defence.

## Table of Contents
- [Organizational Details](#organizational-details)
- [Project Components](#project-components)
- [Setup](#setup)
- [Running the Project](#running-the-project)
- [CI Pipeline](#ci-pipeline)

TODO: steps to run checks from the pipeline (inside each of the projects)

## Organizational Details

- **Course**: CSE2000 Software Project (2023/24)
- **Developed by** group 18A. 
- **Members**: Augustinas Jučas, Victor Purice, Justinas Jučas, Marius Frija, Aldas Lenkšas
- **Client**: Dutch Ministry of Defence
- **Client representative**: Sebastiaan Alvarez Rodriguez
- **Teaching Assistant**: Nathalie van der Werken
- **Coach**: Kubilay Atasu

## Project Components

The application consists of four main parts:
- [`backend`](backend). Backend takes care of reading events from the Kafka topic, processing them, and also exposing an API interface to query the details about ships.
- [`frontend`](frontend). Frontend is the web client written in React.js. It is used to graphically display the relevant information on the map.
- [`simulator`](simulator). Simulator is used to read the historic AIS signal dataset and produce those signals to the Kafka topic.
- [`stress-tester`](stress-tester). Stress tester is needed to perform the Scalability testing.

The project was developed using IntelliJ IDEA IDE. If you want to also use the same IDE, we recommend opening each of the mentioned folders (`backend`, `frontend`, `simulator`, `stress-tester`) as separate projects, instead of opening the root of this repository.
This way, you can leverage starting separate parts using IDE.

Other folders in this repository:
- [`.gitlab`](.gitlab) - GitLab templates for issues and merge requests.
- [`.idea`](.idea) - IntelliJ IDEA project files.
- [`config`](config) - CheckStyle and PMD rulesets.

## Setup

The project was built to work on Linux (Ubuntu 22.04). Different Linux distributions might have slightly different commands than what is written here (depends on the package manager). Please check the official installation guide for each of the mentioned tools in such case.

In case you are using Windows, please use WSL (Windows Subsystem for Linux). In case you are using MacOS, all the written commands and setup should still work. However, we do not take the responsibility of the setup or application not working for the operating systems other than Linux.

The required technologies are:
- [Apache Druid](https://druid.apache.org/), version 29.0.1
- [Apache Kafka](https://kafka.apache.org/), version 3.6.2
- [Node.js](https://nodejs.org/en), version 20.14.0 (latest LTS version)
- [npm (Node.js Package Manager)](https://www.npmjs.com/), version 10.7.0

[Gradle Build Tool](https://gradle.org/) is also used, however gradle wrapper scripts can be run that will automatically download and used the required Gradle version.

### Cloning Repository and Downloding Tools

To clone the repository and download the mentioned tools, you should follow these commands:
```shell
# Of course, you also need to install not only tools, but also the repository itself.
git clone git@gitlab.ewi.tudelft.nl:cse2000-software-project/2023-2024/cluster-w/18a/codebase.git

# From now on, we will assume that the current folder in terminal is 
# one level above the repository (codebase).

# Install nvm (Node Version Manager) and then use it to
# install latest LTS version for Node.js and npm.
# Based on https://nodejs.org/en/download/package-manager.
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
# Before the next command you may need to restart the terminal.
nvm install 20
node -v # Should print `v20.14.0`.
npm -v # Should print `10.7.0`.

# Install the required Apache Druid version.
# Based on https://druid.apache.org/docs/latest/tutorials/.
curl -O https://dlcdn.apache.org/druid/29.0.1/apache-druid-29.0.1-bin.tar.gz 
tar -xzf apache-druid-29.0.1-bin.tar.gz
cd apache-druid-29.0.1 
ls # The folder contains LICENCE, NOTICE, README files, 
   # as well as subfolders for executable files, configuration 
   # files, and more.
cd ..

# Install the required Apache Kafka version.
# Note that this project is using NOT the latest version.
# Based on https://kafka.apache.org/quickstart.
curl -O https://downloads.apache.org/kafka/3.6.2/kafka_2.13-3.6.2.tgz
tar -xzf kafka_2.13-3.6.2.tgz
cd kafka_2.13-3.6.2
ls # The folder contains LICENCE, NOTICE files, as well as 
   # subfolders for executable files, configuration files, 
   # and more.
cd ..
```

## Running the Project

After you have [downloaded the required tools](#setup), you can run them. Note that you may need to use multiple terminal windows for these.

To run the application, you should follow the steps described below in this order. To be more precise, firstly you need to start Druid database (it also starts Zookeeper), then you can start Kafka server. Only when Kafka is running, you can start the backend, and then the frontend. After these, you can also choose to start the simulator or the stress tester.

### 1. Clear previous Kafka and Zookeeper logs

**Note: this is not needed when running the project first time.** In addition, this should not be needed in general, but sometimes
weird bugs can appear because of previously terminated Kafka processes.

To delete the logs, run the following command:
```shell
rm -rf /tmp/kafka-logs /tmp/zookeeper
```

### 2. Start Druid

**Start druid** in one terminal window:
```shell
cd apache-druid-29.0.1 # the installed Druid folder
./bin/start-druid
# If the Druid is running, you can access the web console at
# http://localhost:8888/
```

**Stopping the Druid.**
You can just use `Ctrl+C` in the terminal window where Druid is running.

**Starting Druid in WSL error.**
In WSL, when trying to run Druid you can get a `CANNOT CREATE FIFO` error. This is due to the fact that FIFO file can't be created over mounted drive (`/mnt/c`, for example). An easy solution to this is copying the entire Druid installation folder to any internal folder (`/usr/share`, for example).

**Druid configuration file** is located at `backend/src/main/resources/ship-scores-kafka-supervisor.json`.

### 3. Reset the Druid after previous run

**Note: this step is NOT required when starting the project for the first time.** It is only important when the Druid, Kafka and the backend was started previously, and later backend was closed. In such case, you need to kill the task that
keeps the Druid's connection with the previous process. Additionally, the supervisor can be removed.

**Killing previous tasks.** In the [Tasks tab of the Druid web console](http://localhost:8888/unified-console.html#tasks), you need to select the task that is `Running`, and under the Actions select to kill it.

**[Optional] Removing the previous supervisor.** Usually this is not needed, and can be skipped unless you encounter weird behaviour with the setup. To see the list of 
the supervisors you can either go to the [Supervisors tab of the web console](http://localhost:8888/unified-console.html#supervisors), or run the following command:
```shell
curl "http://localhost:8888/druid/indexer/v1/supervisor"
```
After identifying the id of the supervisor that you want to terminate, you can run the following command:
```bash
curl --request POST "http://localhost:8888/druid/indexer/v1/supervisor/<id>/terminate"
```
Where instead of `<id>` you should write identifier of the supervisor that you want to terminate. Note that the terminated supervisors still exist in the metadata store and their history can be retrieved.
The data is persistently stored on disk in the structure of so-called segments. You can delete them through the [Segments tab of the web console](http://localhost:8888/unified-console.html#segments).

### 4. Create a Druid supervisor for the Kafka topic

When Druid is running, you need to tell the Druid database to retrieve data from the Kafka topic. You can do that by doing 
the following commands. Note that this only needs to be done once (if you stop the Druid and start it again without, and skip the 3rd step, the supervisor is still there).
```shell
cd codebase # go to the project repository folder
curl -X POST -H 'Content-Type: application/json' -d @backend/src/main/resources/ship-scores-kafka-supervisor.json http://localhost:8081/druid/indexer/v1/supervisor
```

When successful, the message `{"id" : "ship-details"}` will be printed in the terminal. In addition, there will be a supervisor named `ship-details` added to the [Supervisors tab of the web console](http://localhost:8888/unified-console.html#supervisors).

### 5. Start Kafka server

Run the commands:
```shell
cd kafka_2.13-3.6.2 # the installed Kafka folder
bin/kafka-server-start.sh config/server.properties
```

### 6. Create (and reset) Kafka topics

**Deleting previous topics.** If you are running the project not the first time, you should delete the previously created
Kafka topics. To do that, you can run:
```shell
cd kafka_2.13-3.6.2 # the installed Kafka folder

bin/kafka-streams-application-reset.sh --application-id anomaly-detection-pipeline --force
bin/kafka-topics.sh --delete --topic ships-raw-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --delete --topic notifications --bootstrap-server localhost:9092
bin/kafka-topics.sh --delete --topic ships-history --bootstrap-server localhost:9092
```

**Creating new topics.** To create the required Kafka topics, run the commands:
```shell
cd kafka_2.13-3.6.2 # the installed Kafka folder

bin/kafka-topics.sh --create --topic ships-raw-AIS --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic notifications --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic ships-history --bootstrap-server localhost:9092
```

### 7. Start the backend

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

### 8. Start the frontend

Follow the instructions in [the section Running Frontend in the file frontend/README.md](frontend/README.md#running-frontend).

### 9. Run the Simulator or Stress Tester

If you finished steps 1-8, then the application is already running. When ship AIS signals come to the `ship-raw-AIS` Kafka topic,
they will be processed in the backend and shown in the frontend. The only missing piece is to have the source for 
the events that are sent to the mentioned Kafka topic. In this repository there are two options for that:
- Simulator that takes the dataset of AIS signals, and sends them to the Kafka topic. To run this, follow [simulator/README.md](simulator/README.md).
- Stress Tester generates the specified amount of signals per second and sends them to the Kafka topic. This is used as a tool for Scalability testing. To run this, follow [stress-tester/README.md](stress-tester/README.md).

**Note** that it is recommended to run only one of the specified appications, either only Simulator or only Stress Tester. This recommendation is given only because these two tools generate ships in different flavour (simulator simulates historic data, whereas stress tester just generates random ships without knowing which places are land and which are water). However, Kafka Messaging Queue can handle multiple producers easily.

## CI Pipeline

This project is using the GitLab CI pipeline which runs jobs on every merge request, and also on `dev` and `main` branches.

The root file for the pipeline configuration is inside [`.gitlab-ci.yml`](.gitlab-ci.yml). All four main components of the application have their own pipeline jobs
to run the tests and ensure code quality:
- [`backend/.gitlab-ci.yml`](backend/.gitlab-ci.yml)
- [`frontend/.gitlab-ci.yml`](frontend/.gitlab-ci.yml)
- [`simulator/.gitlab-ci.yml`](simulator/.gitlab-ci.yml)
- [`stress-tester/.gitlab-ci.yml`](stress-tester/.gitlab-ci.yml)
