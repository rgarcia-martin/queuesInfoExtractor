package com.garciamartir.queues;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.stream.Collectors;

public class JMXInfoExtractor implements InfoExtractor{
    private static final String    WLS_DEFAULT_CONNECTION_FACTORY   = "weblogic.jms.ConnectionFactory";
    private static final String    PROVIDER_URL                     = "%s://%s:%d";
    private static final String    ON_JMSRUNTIME                    = "com.bea:Type=ServerRuntime,Name=%s";
    private static final String[]  WLS_DESTINATION_ATTRIBUTES_NAMES = { "BytesCurrentCount", "BytesHighCount",
            "BytesPendingCount", "BytesReceivedCount",
            "BytesThresholdTime", "CachingDisabled",
            "ConsumersCurrentCount", "ConsumersHighCount",
            "ConsumersTotalCount", "ConsumptionPaused",
            "ConsumptionPausedState", "DestinationType", "InsertionPaused",
            "InsertionPausedState",
            "MessagesCurrentCount",
            "MessagesDeletedCurrentCount",
            "MessagesHighCount",
            "MessagesMovedCurrentCount",
            "MessagesPendingCount",
            "MessagesReceivedCount",
            "MessagesThresholdTime", "Paused",
            "ProductionPaused",
            "ProductionPausedState", "Registered",
            "State" };

    @Override
    public void extractInfo(InfoExtractorArguments arguments) {
        String jmxProtocol = "t3";
        String jmxMBeanServer = "weblogic.management.mbeanservers.runtime";
        HashMap<String, Object> jmxEnv = new HashMap<>();
        jmxEnv.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
        jmxEnv.put("jmx.remote.x.request.waiting.timeout", Long.parseLong("30000")); // 30 secs
        jmxEnv.put(Context.SECURITY_PRINCIPAL, arguments.getUser());
        jmxEnv.put(Context.SECURITY_CREDENTIALS, arguments.getPasswd());
        JMXConnector jmxc = null;
        MBeanServerConnection mbsc = null;

        try {
            JMXServiceURL serviceURL = new JMXServiceURL(jmxProtocol,
                    arguments.getHost(),
                    Integer.parseInt(arguments.getPort()),
                    "/jndi/" + jmxMBeanServer);

            jmxc = JMXConnectorFactory.connect(serviceURL, jmxEnv);
            mbsc = jmxc.getMBeanServerConnection();
            ObjectName serverRuntimeON = new ObjectName(String.format(ON_JMSRUNTIME, arguments.getServerRuntime()));

            ObjectName jmsRuntimeON = (ObjectName) mbsc.getAttribute(serverRuntimeON, "JMSRuntime");
            ObjectName[] jmsServersON = (ObjectName[]) mbsc.getAttribute(jmsRuntimeON, "JMSServers");

            for (ObjectName jmsServerON : jmsServersON) {
                ObjectName[] destinationsON = (ObjectName[]) mbsc.getAttribute(jmsServerON, "Destinations");
                Hashtable<String, Object> attributes = new Hashtable<>();

                for (ObjectName onDestination : Arrays
                        .stream(destinationsON)
                        .filter(destination -> arguments.getJndiFilter().isEmpty() || arguments
                                .getJndiFilter()
                                .stream().anyMatch(destination.getKeyProperty("Name")::contains))
                        .collect(Collectors.toList())) {

                    String destinationName = onDestination.getKeyProperty("Name");
//                    mbsc.invoke(onDestination,"resumeConsumption", null, null);
//                    mbsc.invoke(onDestination,"pauseConsumption", null, null);
                    System.out.printf("Queue: %s%n", destinationName);

                    attributes.put("messagesCurrentCount", mbsc.getAttribute(onDestination, "MessagesCurrentCount"));
                    attributes.put("messagesPendingCount", mbsc.getAttribute(onDestination, "MessagesPendingCount"));
                    attributes.put("consumptionPaused", mbsc.getAttribute(onDestination, "ConsumptionPaused"));
                    attributes.put("insertionPaused", mbsc.getAttribute(onDestination, "InsertionPaused"));
                    attributes.put("productionPaused", mbsc.getAttribute(onDestination, "ProductionPaused"));
                    attributes.put("paused", mbsc.getAttribute(onDestination, "Paused"));
                    System.out.println(attributes);
                }
            }


        } catch (IOException | MalformedObjectNameException | ReflectionException | AttributeNotFoundException | InstanceNotFoundException | MBeanException e) {
            e.printStackTrace();
        } finally {
            try {
                if(jmxc != null) jmxc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
