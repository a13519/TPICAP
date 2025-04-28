package net.zousys.mathtrading.interfaces.icap.config;


import net.zousys.mathtrading.interfaces.Connector;
import net.zousys.mathtrading.interfaces.icap.ICAPConnector;
import net.zousys.mathtrading.interfaces.icap.ICAPMessageRepo;
import net.zousys.mathtrading.interfaces.icap.ServerSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectorConfig {
    @Value("${app.connection.host}")
    private String host;
    @Value("${app.connection.port}")
    private int port;
    @Value("${app.connection.key}")
    private String key;
    @Value("${app.connection.value}")
    private String value;
    @Value("${app.connection.ssl}")
    private Boolean ssl;
    @Autowired
    private ICAPMessageRepo icapMessageRepo;
    /**
     *
     * @return
     */
    @Bean
    public Connector[] connectors() {
        return new Connector[]{
                new ICAPConnector(
                        ServerSignature.builder()
                                .ssl(ssl)
                                .host(host)
                                .port(port)
                                .key(key)
                                .value(value).build(), icapMessageRepo
                )
        };
    }
}
