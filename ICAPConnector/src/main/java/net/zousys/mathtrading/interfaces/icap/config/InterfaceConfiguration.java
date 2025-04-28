package net.zousys.mathtrading.interfaces.icap.config;

import net.zousys.mathtrading.interfaces.Connector;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.Pusher;
import net.zousys.mathtrading.interfaces.icap.ICAPConnector;
import net.zousys.mathtrading.interfaces.icap.ICAPProcessor;
import net.zousys.mathtrading.interfaces.icap.ServerSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

@Configuration
public class InterfaceConfiguration {

    @Value("${app.pool.connector}")
    private int poolConnector;
    @Value("${app.pool.processor}")
    private int poolProcessor;


    /**
     *
     * @return
     */
    @Bean
    public ExecutorService collectorService(){
        return Executors.newFixedThreadPool(poolConnector);
    }

    /**
     *
     * @return
     */
    @Bean
    public ExecutorService monitorService(){
        return Executors.newFixedThreadPool(1);
    }

    /**
     *
     * @return
     */
    @Bean
    public ExecutorService processorService(){
        return Executors.newFixedThreadPool(poolProcessor);
    }

    /**
     *
     * @return
     */
    @Bean
    public Flow.Subscriber<Message> subscriber() {
        return new ICAPProcessor();
    }
}
