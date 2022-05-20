package com.garciamartir.queues;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class InfoExtractorArguments {
    private String host;
    private String port;
    private String user;
    private String passwd;
    private String serverRuntime;
    private List<String> jndiFilter;
}
