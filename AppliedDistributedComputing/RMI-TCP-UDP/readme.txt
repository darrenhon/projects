1. Executive Summary

Assignment Overview

  This project gives some hands on experience in using RPC, particularly Java RMI, for interactions between server and client. The previous project is about socket programming, which has to deal with a lot of low level details. This project, in contrast, focus more on more high level features like handling multiple client connections and concurrency.
  One important part of this project is to handle concurrent client calls to the RMI object. This is a real world requirement where clients will not line up one by one to request services from server. The encapsulation provided by RMI comes very handy because it hides all socket level multithread connections handling. We don't have to care about how different clients are connected to the server. This is a major benefit of using an RPC framework over socket programming. To avoid conflicts of concurrent client calls, we have to use mutex (or locks) to ensure atomic operations in some part of the code that does not allow concurrent execution. This is also a real world situation where multiple clients may contend for a shared resource and we have to make sure only one of them gain access at a time.
  By using RMI, we learned how to construct interface for remote objects, create concrete class that implements the interface, register remote objects in RMI registry, retrieve the remote object stub from the client, and invoke methods of remote objects from client. 
  By extending project 1 into multithreading, we learned multi-threaded programming, synchronization between threads, threads creation and termination. These low level socket programming techniques are fundamental building blocks for higher level distributed systems programming.

Technical Impression

  To me, this project is not entirely technically challenging, but several thoughts come to my mind when working on it. They are listed in bullet points:

- For the requirement of multiple client connection, it is hard to test on our side. We do not have a network of computers to simulate such use case. I tested it by opening multiple command line terminals and running clients on each of them. I also have to modify the client to continuously emit commands to the server to simulate conflicting invocations. But it is still far from real multi-client situations.
- As the project doesn't specify whether the RMI registry has to be the same machine as the server process, I assumed they are on the same machine.
- As the project doesn't specify whether the port number has to be customizable, I skipped this feature and use the default port 1099 for RMI registry. 
- There was a lot of confusion as the project requirements mentioned a use of multithreading but did not mention extending the code of project 1. It was written as if we should use multithreading in the RMI server. 

2. Instructions

You need Java 8 to build and run the programs.
Make sure port 1099 is not blocked by firewall.
Build the source code first by running "make".

Start the RMI registry by runnning the following command:
rmiregistry
This is the command for Mac. 

To run RMI server, start the RMI registry first, then run the following command:
java RMIServer

To run RMI client, start RMI registry and RMI server first, then run the following command:
java RMIClient server-address

To run TCP server, run the following command:
java TCPServer server-port [max-client]
Choose a port not blocked by firewall.
Optional: max-client is the maximum number of clients the TCPServer can serve simultaneously. Default is 5.

To run TCP client, start TCP server first, then run the following command:
java TCPClient server-address server-port

To run UDP server, run the following command:
java UDPServer server-port
Choose a port not blocked by firewall.

To run UDP client, start UDP server first, then run the following command:
java UDPClient server-address server-port

