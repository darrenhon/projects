1. Executive Summary

Assignment Overview

  The main tasks of this project include extending the single server from project 2 into multiple servers, as well as an implementation of the 2-phase commit protocol. By working on these tasks we gained invaluable experience in designing scalable inter-server communication and server-client communication. 
  Extending from one server into multiple is a huge task involving many subtasks and design considerations. First, each server has to be able to discover the other servers. Second, each server has to maintain a list of servers that are currently running. When one server is started, it has to notice all existing servers so that all of them will have the most updated list. When each server is shutting down, it has to inform the others too. The process of keeping servers acknowledging the presence of each other is not trivial.
  Implementing 2 PC is also a big task. It is because data has to be replicated between servers. Whenever each server receives an update command, it has to go through the 2 PC protocol to make sure all servers either commit or abort the transaction. In 2PC, there are a lot of communications between the servers. When a transaction is initiated, each server has to be very careful with the locks that it is manipulating, because calls from clients and servers may be interleaved and deadlocks are very likely to happen.
  So after doing this project, we learned how to maintain a distributed architecture with multiple servers and clients, the design of a high availability system by using replication, and the principle of distributed transaction by 2 PC.

Technical Impression

  To me, this project is not entirely technically challenging, but several thoughts come to my mind when working on it. They are listed in bullet points:

- For the requirement of multiple clients and servers, it is hard to test on our side. We do not have a network of computers to simulate such use case. I tested it by opening multiple command line terminals and running clients and servers on each of them. I also have to modify the client to continuously emit commands to the server to simulate conflicting invocations. But it is still far from real multi-client situations.
- When I was testing multiple clients and servers, a lot of deadlocks were experienced and I spent a great deal of time resolving them. It is because different parts of the servers require different locks.
- After implementing 2PC across multiple servers, I found that the throughput is much worse than a single server in project 2. It is because the servers now need to go through a lot of communications, lock acquiring, log writing, persistence storage access, etc. before a transaction is actually done.

2. Instructions

You need Java 8 to build and run the programs.
Make sure port 1099 is not blocked by firewall.

2.1 Build the source code
Build the source code first by running "make".
After the build, the Java classes are copied to server1, server2, server3, server4, and server5 folders.

2.2 Start RMI registry
Start the RMI registry by running the following command:
rmiregistry
This is the command for Mac. 
This project assumes all servers are sharing the same RMI registry. So if you're running multiple servers on different machines, you need to specify the RMI registry address when starting the servers.

2.3 Start RMIServer
To run RMI server, start the RMI registry first, then run the following command:
java RMIServer [rmiregistry-address]

If "rmiregistry-address" is not specified, the default is localhost.
If you are running multiple servers on different machines, select one to host the RMI registry and specify that address when starting other servers.
If you are running multiple servers on the same machine, make sure each server is running in a different folder because each server will save DB, log and transaction log with the same filenames. I already created 5 folders to hold the Java classes. You can go into each folder and start the server inside.

2.4 Start RMIClient
To run RMI client, start RMI registry and RMI server first, then run the following command:
java RMIClient [rmiregistry-address] [server-name]

If "rmiregistry-address" is not specified, the default is localhost.
If "server-name" is not specified, the default is Server1.
If you are running multiple servers, each server has a server name "Server#" which you can see when starting the server.
If you want to connect to a specific server in a group of servers, specify the "server-name". Mind the case, it is capital "S" in "Server#".

After starting the RMIClient, you're asked if you want to pre-populate the server. This is because for multiple clients, you may not want to pre-populate it every time you start a client. If you select no, you can start entering commands.
