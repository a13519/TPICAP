package net.zousys.mathtrading.interfaces.tpicap.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.tpicap.model.MsgClassifier;
import net.zousys.mathtrading.interfaces.tpicap.model.ServerSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class EssentialConfig {
    @Value("${app.pool.processor}")
    private int poolProcessor;
    @Value("${app.pool.connector}")
    private int poolConnector;
    @Value("${app.pool.recorder}")
    private int poolRecorder;
    @Value("${app.timezone}")
    private String timezone;
    @Value("${app.connection.proxyHost:null}")
    private String proxyHost;
    @Value("${app.connection.proxyPort:-1}")
    private int proxyPort;
    @Value("${app.connection.host}")
    private String host;
    @Value("${app.connection.port}")
    private int port;
    @Value("${app.connection.key}")
    private String key;
    @Value("${app.connection.value}")
    private String value;
    @Value("${app.connection.ssl}")
    private boolean ssl;
    /**
     * @return
     */
    @Bean
    public ExecutorService monitorService() {
        return Executors.newSingleThreadExecutor();
    }

    /**
     * @return
     */
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

    @Bean
    public ExecutorService connectorService() {
        return Executors.newFixedThreadPool(poolConnector);
    }

    @Bean
    public MsgClassifier classifier() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("business-types.properties")) {
            if (inputStream == null) {
                log.error("File not found: business-types.properties");
            }
            properties.load(inputStream);
            String types = properties.getProperty("types");
            String exclusion = properties.getProperty("exclusion");
            return new MsgClassifier(
                    new HashSet<>(Arrays.stream(types.split(",")).map(String::trim).toList()),
                    new HashSet<>(Arrays.stream(exclusion.split(",")).map(String::trim).toList())
            );
        } catch (Exception e) {
            log.error("Error thrown when loading business message types: " + e.getLocalizedMessage());
        }
        return new MsgClassifier(new HashSet<>(), new HashSet<>());
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "app.tracing.message")
    public static class EnumConfig {
        private Constants.ContentLevel contentLevel;
        private Constants.SerializeLevel serializeLevel;
    }

    @Bean
    public ServerSignature serverSignature() {
        return ServerSignature.builder()
                .ssl(ssl)
                .host(host)
                .port(port)
                .key(key)
                .value(value)
                .proxyHost(proxyHost)
                .proxyPort(proxyPort).build();
    }
    @Bean
    public ZoneId zoneId() {
        return ZoneId.of(timezone);
    }
}
