package net.zousys.mathtrading.interfaces.icap.config;

import com.icap.iConnect.srcMsgs.enums.EICTradeRequest;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgTradeRequest;
import net.zousys.mathtrading.interfaces.Connector;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.icap.*;
import net.zousys.mathtrading.interfaces.icap.tracing.ICMessageRecorder;
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
    @Autowired
    private ICAPSessionManager icapSessionManager;
    /**
     * @return
     */
    @Bean
    public Flow.Subscriber<Message> subscriber() {
        return new ICAPProcessor();
    }

    /**
     * @return
     */
    @Bean
    public List<ICAPMessage> initMsgs() {
        List<ICAPMessage> msgs = new ArrayList<>();
        msgs.add(new ICAPMessage(new ICMsgTradeRequest(EICTradeRequest.eTradeRequestUnmatched, "")));
        return msgs;
    }

    /**
     * @return
     */
    @Bean
    public Connector[] connectors() {
        return new Connector[]{
                new ICAPConnector(
                        icapSessionManager,
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