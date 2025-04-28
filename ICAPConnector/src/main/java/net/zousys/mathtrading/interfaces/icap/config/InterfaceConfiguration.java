package net.zousys.mathtrading.interfaces.icap.config;

import com.icap.iConnect.srcMsgs.enums.EICTradeRequest;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgTradeRequest;
import net.zousys.mathtrading.interfaces.Connector;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.icap.ICAPConnector;
import net.zousys.mathtrading.interfaces.icap.ICAPMessageRepo;
import net.zousys.mathtrading.interfaces.icap.ICAPProcessor;
import net.zousys.mathtrading.interfaces.icap.ServerSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

@Configuration
public class InterfaceConfiguration {

    @Value("${app.pool.connector}")
    private int poolConnector;
    @Value("${app.pool.processor}")
    private int poolProcessor;
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

    /**
     *
     * @return
     */
    @Bean
    public List<ICMsg> initMsgs() {
        List<ICMsg> msgs = new ArrayList<>();
        ICMsg msg = new ICMsgTradeRequest(
                EICTradeRequest.eTradeRequestUnmatched, "");
        msgs.add(msg);
        return msgs;
    }
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
                                .value(value).build(), icapMessageRepo, initMsgs()
                )
        };
    }
}
