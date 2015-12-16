1. Executive Summary

Assignment Overview

  The main tasks of this project include transforming the 2PC protocol from project 3 to the Paxos algorithm for distributed consensus, and changing the communication paradigm from synchronous to asynchronous. The purpose is to give us the experience of implementing Paxos from the ground up and develop a deep understanding of how distributed systems achieve consensus while tolerating fail-stop failures.
  Paxos is widely used as industry standard for distributed transaction and consensus algorithm in many production environment of large scale business systems. It is therefore very important for students to get familiar with this algorithm and get some experience on its implementation.
  To start this project, we have to read numerous papers and references on Paxos, which is already a learning process. Different papers and materials described Paxos from different perspective and include different parts in their illustrations. It makes us think in different angles of what really are the essences of a distributed consensus protocol.
  When we are implementing the algorithm, one of the big tasks is to convert synchronous paradigms from previous projects into asynchronous. It is because when a client sends write request to Proposer, the proposer will take indefinite time to process the request, and the result, if any, is given by the Learner, not the Proposer. This is a multi-party unbounded communication that is not practical to be done synchronously. By implementing asynchronous communication between server roles and clients, all deadlock issues happened in previous projects were eliminated naturally because there is no more waiting for each remote method call. But the challenges will be shifted to how each server role process requests with a thread. This is also an important part that we learned from this project.
  So after doing this project, we went through independently study of a complex algorithm, understood the essential parts of a fault tolerant consensus algorithm, and gained valuable experience of implementing distributed asynchronous communication protocol.

Technical Impression

  To me, this project is not entirely technically challenging, but several thoughts come to my mind when working on it. They are listed in bullet points:

- The Paxos we discussed in class is oversimplified. In practice there are many more considerations that were not discussed in the lessons. For example, in a distributed database, we are not dealing with consensus of one value at a time but many many values simultaneously. So each role in Paxos will have to keep track of a list of values that is currently undergoing Paxos. This is not an easy task.
- Also Paxos is only concerned about arriving consensus for a single value in a single write operation. It does not take into account of a set of operations involving reads and writes, and the atomicity of transaction.
- The provided materials do not agree on how read operation is implemented. Some suggest read can be done to database servers directly without going through Proposers, some do not mention at all. So I implemented a majority voting on read operation. 
- Implementing asynchronous communication is particularly challenging because it involves building a task queue in each server role and having a background thread to poll the queue and process the task. I have to make the framework general enough to accommodate all types of asynchronous operations between servers and clients.

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
This project assumes all servers and clients are sharing the same RMI registry. So if you're running multiple servers on different machines, you need to specify the RMI registry address when starting the servers.

2.3 Start Server instances
Each server instance can consist of multiple roles, each role can have multiple members.
There are totally 4 roles: DBServer, Proposer, Acceptor and Learner.
You can start at most 10 members for each role. But make sure there is at least one member for each role, otherwise the system will not work.
If you want to start multiple DBServer, each of them must be in different folders because DBServer will write data into persistence storage. If there are more than 1 DBServer running in the same folder, there will be conflict writing to the file. This restriction does not apply to other roles. 
Start server instance by the following command:
java Server [rmi-registry-address] Role [Role] [Role] ..

If "rmiregistry-address" is not specified, the default is localhost.

e.g. java Server localhost DBServer Acceptor Acceptor Acceptor Learner Learner Learner
This command starts 1 DBServer, 3 Acceptor and 3 Learner in the same server instance

e.g. java Server localhost Proposer Proposer Proposer
This command starts 3 Proposer in the same server instance

2.4 Start Client
To run client, start RMI registry and all server roles first, then run the following command:
java Client [rmiregistry-address]

If "rmiregistry-address" is not specified, the default is localhost.

After starting the Client, you're asked if you want to pre-populate the server. This is because for multiple clients, you may not want to pre-populate it every time you start a client. If you select no, you can start entering commands.
