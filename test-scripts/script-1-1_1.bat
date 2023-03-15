rem Start 1 network node, then find a key using it
timeout 1 > NUL
start java -cp ..\compiled\ DatabaseNode -tcpport 9000 -record 1:1 
timeout 3 > NUL
java -cp ..\compiled\ DatabaseClient -gateway localhost:9000 -operation get-value 1
java -cp ..\compiled\ DatabaseClient -gateway localhost:9000 -operation terminate
timeout 10 > NUL