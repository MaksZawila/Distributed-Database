rem Start 2 network nodes, find min/max and then terminate them
timeout 1 > NUL
start java -cp ..\compiled\ DatabaseNode -tcpport 9000 -record 1:1 
timeout 1 > NUL
start java -cp ..\compiled\ DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:2 
timeout 1 > NUL

java -cp ..\compiled\ DatabaseClient -gateway localhost:9000 -operation get-max
java -cp ..\compiled\ DatabaseClient -gateway localhost:9000 -operation get-min
timeout 1 > NUL

java -cp ..\compiled\ DatabaseClient -gateway localhost:9000 -operation terminate
java -cp ..\compiled\ DatabaseClient -gateway localhost:9001 -operation terminate
timeout 10 > NUL