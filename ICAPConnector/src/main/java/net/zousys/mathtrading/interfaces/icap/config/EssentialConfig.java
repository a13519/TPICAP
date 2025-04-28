package net.zousys.mathtrading.interfaces.icap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Configuration
public class EssentialConfig {
    @Value("${app.pool.connector}")
    private int poolConnector;
    @Value("${app.pool.processor}")
    private int poolProcessor;
    @Value("${app.pool.recorder}")
    private int poolRecorder;
    /**
     * @return
     */
    @Bean
    public ExecutorService collectorService() {
        return Executors.newFixedThreadPool(poolConnector);
    }

    /**
     * @return
     */
    @Bean
    public ExecutorService monitorService() {
        return Executors.newFixedThreadPool(1);
    }

    @Bean
    public ExecutorService recorderService() {
        return Executors.newFixedThreadPool(poolRecorder);
    }

    /**
     * @return
     */
    @Bean
    public ExecutorService processorService() {
        return Executors.newFixedThreadPool(poolProcessor);
    }

}
