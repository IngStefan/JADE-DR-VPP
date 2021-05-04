# JADE-DR-VPP
## What is this repo or project?
This project consists of different software parts for Demand Response (DR) Aggregators or Virtual Power Plant (VPP) Operators to utilize a Multi-agent System (MAS) by realizing DR mechanisms of the German DR markets.

Additional documentation for this project will be released this year (2021).

## How does it work?
The following figure shows the different parts of the project and how to use them.

![Project Overview](https://github.com/IngStefan/JADE-DR-VPP/blob/994865fe4fb02b1f547879d53f40a40e0cf5f291/GitHubOverview.png)

The Java projects are **Spring-Boot** applications. The *Startup.java* is the main type of each application.

### JADE_VPP
The core of the application is the **JADE_VPP** project, which can also be used as a standalone version to test the agent system.
For this the desired agents must be instantiated in the *Startup.java* in the platform (at least one VPP agent).
Then the Spring-Boot project can be started (Main Type: mas.JADE_VPP.Startup).
The platform starts sending data directly to the frontend (Node-RED Flow: VPP-VPP-Agent (A)), so this should already be active.
Dedicated TU agents that connect to frontends D and E can also be generated from this program. For this, the *ContainerManager.java* must then be adapted and the Main Container specified.

### TU_Simulation
The **TU_Simulation** can be used to run an external agent container. Necessary settings for the agent composition can be done in the *Startup.java* and the *ContainerManager.java*, to which the TUs should connect, must be configured in the ContainerManager.java (IP address).
This program is modified, so that data for the TU agents are not exchanged over the frontend (TU side), but they are pre-generated.

### Simulation
The **Simlation** application can be used to realize a simulation scenario via the VPP frontend. Here the simulation can be adapted in the *startup.java* and the Node-RED frontend must be prepared accordingly.

## Who will use this repo or project?
This project is especially created for DR Aggregators or VPP Operators that realize DR mechanisms (of the German market).
Otherwise, anyone who is interested in MAS or is active in the research field is welcome to use this software.

## What is the goal of this project?
To help DR Aggregators use MAS to improve their existing VPPs.

## Additional documentation
More to come at the end of 2021
* [Technical implementation of this system](https://ieeexplore.ieee.org/document/9212168)


