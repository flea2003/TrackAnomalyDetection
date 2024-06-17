# Stress Testing Simulator

The stress tester is used to test 
the application by simulating a large number of ships sending a large number of signals
to the system. It differs from the main [simulator](../simulator) in the sense that it does not generate
data from a real source, but rather generates **a lot** of random data. The only purpose of this data 
is to see how much load the system (both the frontend and the backend) can handle.

## Table of Contents
- [Running Stress Tester](#running-stress-tester)
- [Running Static Checks](#running-static-checks)

## Running Stress Tester

### 1. Setting the options

The default options are already set, but if you want to change them, you can do so by changing
the variables at the top of the [Main.java](src/main/java/stresstester/Main.java).
The variables to customize are:
- `topicName` specifies the Kafka topic to which the ship AIS signals are send.
- `serverName` specifies the Kafka server URL (and port).
- `minIntervalBetweenMessages` and `maxIntervalBetweenMessages` specify the bounds of the time between one ship's messages. The time interval to send next signal for a ship is selected randomly between these bounds for each event.
- `signalsPerSecond` specifies how many signals are sent per second in total **on average** (summed over all ships). **This is the value that you should focus on changing.**
- `threadCount` specifies how many threads will be used for parallelized sending AIS signals.

### 2. Starting the tester

**Before starting the stress tester, you need to set up and start the Druid and Kafka.** The instructions can be found in the [README.md in the folder above](../README.md). 

Then, you can start the stress tester by running the following commands:
```shell
cd codebase/stress-tester
chmod +x ./gradlew # Make the wrapper script executable.
./gradlew clean    # Optional. Run if you want to clean the previous build.
./gradlew build    # Build the project; also runs tests and static analysis.
./gradlew run 
```

## Running Static Checks

Note that this project is itself a test (and only for internal usage), therefore it
does not have automated tests.

To ensure code quality of this tester, the following tools are used (also included in the GitLab pipeline):
- [CheckStyle](https://checkstyle.sourceforge.io/) code style checker.
- [PMD](https://pmd.github.io/) static code analyzer.

You can run them using the following commands:
```shell
cd codebase/stress-tester
chmod +x ./gradlew       # Make the wrapper script executable.

./gradlew checkstyleMain # Runs checkstyle, the report is generated at 
                         # codebase/stress-tester/build/reports/checkstyle/main.html.
                         
./gradlew pmdMain        # Runs PMD, the report is generated at
                         # codebase/stress-tester/build/reports/pmd/main.html.
```


