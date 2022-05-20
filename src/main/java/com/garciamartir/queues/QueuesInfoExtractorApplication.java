package com.garciamartir.queues;

import java.util.ArrayList;
import java.util.List;

public class QueuesInfoExtractorApplication {
    public static void main(String[] args) {
        if(args.length < 5){
            System.out.println("Error, Usage: <server> <port> <username> <password> <serverRuntime> [jndi[ ,jndi[ ,jndi ...]]]");
        }

        String host = args[0];
        String port = args[1];
        String username = args[2];
        String password = args[3];
        String serverRuntime = args[4];
        List<String> jndiFilter = new ArrayList<>();
        for(int i = 5; i < args.length; i++){
            jndiFilter.add(args[i]);
        }

        InfoExtractorArguments arguments = new InfoExtractorArguments(host, port, username, password, serverRuntime, jndiFilter);

        InfoExtractor extractor = new JMXInfoExtractor();
        extractor.extractInfo(arguments);
    }
}
