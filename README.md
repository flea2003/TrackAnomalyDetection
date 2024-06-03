# Anomaly Detection Pipeline
This project contains the source code for the Anomaly Detection Pipeline developed by team 18A
for the course *Software Project* at TU Delft. It has been developed for the Dutch Ministry of
Defence.

## Technical documentation
The project contains 3 main parts:
- The simulator - a Java project that reads from a dataset file and produces signals to a Kafka topic.
- The backend - a Java project that reads from a Kafka topic, processes the signals, calculates anomaly information and exposes
an interface for querying the scores and ship positions.
- The web client - a React project that queries the pipeline for scores and ship positions and displays all relevant information,
mainly in the form of a map.

All 3 parts are extensively documented in their respective README files. Please check out the following README files
for much more information:
- [backend](backend/README.md)
- [frontend](frontend/README.md)
- [simulator](simulator/README.md)

Additionally, there is a [stress-tester](stress-tester/README.md) Java project that acts as a manually-runnable test for the backend. Starting it is as simple as running the
Gradle project. It is technically for internal usage only.

### Building the project
For detailed instructions how to build the project, check out the README files of the individual parts of the project.
In general building the full project should require Java 17, Gradle, Node.js.

### Running the project
In order to start the project, first start the backend (using all of the instructions from the README file), then start
the simulator, and finally start the frontend. The frontend will become available at *localhost:3000*.


