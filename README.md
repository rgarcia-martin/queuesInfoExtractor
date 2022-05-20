Java 8 App to extract JMS Queues information from Weblogic JMX.

To Build with Maven:

```mvn clean compile assembly:single```

To Launch the Jar:

```java -jar queuesInfoExtractor.jar host port username password serverRuntime [queueName [queueName [...]]]```

And you will get some info about the queues like that:

````
Queue: SystemModuleName!JMSServerName@ClusterName QueueName1
{paused=false, insertionPaused=false, messagesCurrentCount=0, consumptionPaused=true, productionPaused=false, messagesPendingCount=0}
````
