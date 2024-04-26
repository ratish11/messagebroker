# Distributed Messaging System

## Overview

This project implements a distributed messaging system comprised of a central coordinator and multiple participants. The coordinator manages participant registration, deregistration, and message routing. Participants can register with the coordinator, send and receive messages, and manage their connections (connect/reconnect/disconnect) as needed.

## Installation

**Prerequisites:**

* Java installed on your system. You can verify this by running `java -version` in your terminal. If you don't have Java, download and install it from the official website: https://www.oracle.com/java/technologies/downloads/

**Steps:**

1. **Clone the Repository:**

   ```bash
   git clone [https://github.com/your/repository.git](https://github.com/your/repository.git)
   ```
2. **Compile the Java Files:**

Navigate to the project directory and compile the Java source files:
```bash
cd your/repository/directory
javac coordinator.java participant.java
```

3. **Run the Coordinator:**

Start the coordinator using the configuration file path as an argument:
```bash
java coordinator <config_file>
```
4. **Run Participants:**
Open a new terminal window and navigate to the project directory. Start participants individually using the same configuration file:
```bash
java participant <config_file>
```
Usage

Once the coordinator and participants are running, use the following commands to interact with the system:

Participant Management:

    register <port>: Registers a participant with the coordinator on the specified port.
    deregister: Deregisters the currently connected participant.
    reconnect <port>: Reconnects a previously registered participant to the coordinator on the specified port.
    disconnect: Disconnects the currently connected participant.

Message Sending:

    msend <message>: Sends a message to all connected participants. This command should be executed from the coordinator terminal.

Configuration

Each participant requires a configuration file containing the following information:

    Participant ID: A unique identifier for the participant.
    Log File Name: The name of the file where logs will be written.
    Coordinator Hostname/IP: The hostname or IP address of the coordinator machine.
    Coordinator Port: The port number on which the coordinator is listening.

Example Configuration File (config.txt):
```
participant1 
log.txt 
localhost 
9000
```
Replace the placeholder values with your specific configuration details.
Contributors

    Ratish Jha
    Soumya Bharadwaj


