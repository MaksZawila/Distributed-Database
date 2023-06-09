rem Start 3 network nodes, then terminate them
start java -cp ..\compiled\ DatabaseNode -tcpport 9000 -record 1:1 
timeout 1 > NUL
start java -cp ..\compiled\ DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:2 
timeout 1 > NUL
start java -cp ..\compiled\ DatabaseNode -tcpport 9002 -connect localhost:9000 -connect localhost:9001 -record 3:3
timeout 1 > NUL
java -cp ..\compiled\ DatabaseClient -gateway localhost:9001 -operation get-min
java -cp ..\compiled\ DatabaseClient -gateway localhost:9001 -operation get-max
timeout 1 > NUL
java -cp ..\compiled\ DatabaseClient -gateway localhost:9001 -operation set-value 1:4
java -cp ..\compiled\ DatabaseClient -gateway localhost:9001 -operation get-max
timeout 1 > NUL
java -cp ..\compiled\ DatabaseClient -gateway localhost:9001 -operation set-value 3:1
java -cp ..\compiled\ DatabaseClient -gateway localhost:9001 -operation get-min

java -cp ..\compiled\ DatabaseClient -gateway localhost:9000 -operation terminate
java -cp ..\compiled\ DatabaseClient -gateway localhost:9001 -operation terminate
java -cp ..\compiled\ DatabaseClient -gateway localhost:9002 -operation terminate
timeout 10 > NUL