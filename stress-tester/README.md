# Stress Testing Simulator

This small Gradle project is for internal use only. Its purpose is to stress test 
the application by simulating a large number of ships sending a large number of signals
to the system. It differs from the main simulator in the sense that it does not generate
data from a real source, but rather generates **a lot** of random data. The only purpose of this data 
is to see how much load the system (both the frontend and the backend) can handle.

Note that this project is itself a test (and only for internal usage), therefore it
does not have automated tests.

## Building the project
Since this is a Gradle project, simply run `./gradlew build` to build the project. In case of 
running from Intellij, just open the project and let it build automatically.


## Running the project
Run the project by executing the `./gradlew bootRun` command. This will start the stress-test simulator.

Note that before starting this project, the backend and Kafka should be started and the required topics created. Please follow the
detailed instructions in the `../backend` folder on how to start Kafka and the backend and what topics to create.