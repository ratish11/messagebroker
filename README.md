
# Distributed Messaging System

## Overview

This project implements a distributed messaging system consisting of a coordinator and multiple participants. The coordinator manages the registration, deregistration, and message routing among participants. Participants can register with the coordinator, send and receive messages, and disconnect/reconnect as needed.

## Installation

To run the system, you need Java installed on your system. Follow these steps:

1. Clone the repository:

   ```bash
   git clone https://github.com/your/repository.git
```



Compile the Java files:

```bash

javac coordinator.java participant.java```

Run the coordinator:

```bash

java coordinator <config_file> ```

Run participants:

bash

    java participant <config_file>

Usage

Once the coordinator and participants are running, you can use the following commands:

    register <port>: Registers a participant with the given port.
    deregister: Deregisters the participant.
    reconnect <port>: Reconnects the participant to the given port.
    disconnect: Disconnects the participant.
    msend <message>: Sends a message to all connected participants.

Configuration

Each participant needs a configuration file containing the following information:

    Participant ID
    Log file name
    Hostname/IP address of the coordinator
    Port number

Example configuration file (config.txt):

participant1
log.txt
localhost
9000

Contributors

    Ratish Jha
    Soumya Bharadwaj
