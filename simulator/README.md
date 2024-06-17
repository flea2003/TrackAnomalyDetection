# Simulator documentation
Simulator is an application for simulating the stream of AIS signals that should be sent by actual ships. This application turns a list of hardcoded AIS signals to an actual stream, where each signal is appended to a certain Kafka topic at its corresponding time.

## Table of Contents
- [Running Simulator](#running-simulator)
- [Running Tests and Static Checks](#running-tests-and-static-checks)

## Running Simulator

### 1. Selecting the dataset

You should create the folder `streaming_data` (inside the `codebase/simulator`).
In this folder you should add a wanted dataset file. The default dataset that we have been using can be downloaded from https://www.marinetraffic.com/research/dataset/marinetraffic-automatic-identification-system-ais/.

If another dataset which has a different format is added, a new parser class should be implemented in [`parsers`](src/main/java/parsers) directory. It should implement the interface [`Parser`](src/main/java/parsers/Parser.java). You can check how [the current parser](src/main/java/parsers/DEBSParser.java) is implemented.

### 2. Setting the options

**Note that the default options are already set. You can skip this step if you don't want to use other options.**

In the method [`main()`] of the class [Main](src/main/java/simulator/Main.java) you can change the variables to change how the simulator behaves:
- `topicName` specifies the Kafka topic to which the simulator sends the ship events.
- `serverName` specifies the Kafka server URL (and port).
- `dataSetName` specifies the filename of the dataset used. This dataset should be put inside [`streaming_data`](streaming_data) folder.
- `startTime` and `endTime` specify the time interval from which the signals are taken.
- `simulatorSpeed` specifies how fast the signals are sent relatively to their historic time of appearance.
- `parser` specifies which dataset parser is used.

### 3. Starting Simulator

**Before starting the simulator, you need to set up and start the Druid and Kafka.** The instructions can be found in the [README.md in the folder above](../README.md). You also must download and specify the dataset ([step 1](#1-selecting-the-dataset)).

Then, you can start the simulator by running the following commands:
```shell
cd codebase/simulator
chmod +x ./gradlew # Make the wrapper script executable.
./gradlew clean    # Optional. Run if you want to clean the previous build.
./gradlew build    # Build the project; also runs tests and static analysis.
./gradlew run 
```

Or you can start the [`Main` class](src/main/java/simulator/Main.java) in IDE.

## Running Tests and Static Checks

To ensure code quality of the simulator code, the following tools are used (also included in the GitLab pipeline):
- [JUnit](https://junit.org/junit5/) and [AssertJ](https://assertj.github.io/doc/) frameworks for unit testing.
- [JaCoCo (Java Code Coverage)](https://www.eclemma.org/jacoco/) for analyzing the coverage. 
- [PIT Mutation Testing](https://pitest.org/) framework.
- [CheckStyle](https://checkstyle.sourceforge.io/) code style checker.
- [PMD](https://pmd.github.io/) static code analyzer.

You can run them using the following commands:
```shell
cd codebase/simulator
chmod +x ./gradlew              # Make the wrapper script executable.

./gradlew test jacocoTestReport # Runs unit tests, the JaCoCo report is generated at
                                # codebase/simulator/build/reports/jacoco/test/html/index.html.

./gradlew pitest                # Runs mutation testing, the report is generated at
                                # codebase/simulator/build/reports/pitest/index.html.

./gradlew checkstyleMain        # Runs checkstyle, the report is generated at 
                                # codebase/simulator/build/reports/checkstyle/main.html.
                         
./gradlew pmdMain               # Runs PMD, the report is generated at
                                # codebase/simulator/build/reports/pmd/main.html.
```
   

