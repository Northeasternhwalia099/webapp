package com.csye.webapp.configuration;

import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class statsD {

    @Value("true")
    private boolean Message_publish;

    @Value("localhost")
    private String metric_Host;

    @Value("8125")
    private int port_Number;

    @Value("csye6225")
    private String prefix_;

    @Bean
    public StatsDClient metricClient() {
        if (Message_publish)
       
            return new NonBlockingStatsDClient(prefix_, metric_Host, port_Number);
        
        return 
                new NoOpStatsDClient();
    }

}
