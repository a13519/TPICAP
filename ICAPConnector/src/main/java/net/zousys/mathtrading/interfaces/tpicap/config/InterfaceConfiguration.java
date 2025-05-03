package net.zousys.mathtrading.interfaces.tpicap.config;

import com.icap.iConnect.srcMsgs.enums.EICTradeRequest;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgOrderBookRemove;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgTradeBookRemove;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgTradeRequest;
import net.zousys.mathtrading.interfaces.tpicap.ICAPConnector;
import net.zousys.mathtrading.interfaces.tpicap.ICAPMessage;
import net.zousys.mathtrading.interfaces.tpicap.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class InterfaceConfiguration {
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
    private Boolean ssl;
    @Autowired
    private ICAPMessageRepo icapMessageRepo;
    @Autowired
    private ICAPDispatchQueue icapDispatchQueue;
    @Autowired
    private ServerStatus serverStatus;

    /**
     * @return
     */
    @Bean
    public List<ICAPMessage> initCommands() {
        List<ICAPMessage> msgs = new ArrayList<>();
        msgs.add(new ICAPMessage(new ICMsgTradeRequest(EICTradeRequest.eTradeRequestUnmatched, "")));
        return msgs;
    }

    /**
     * @return
     */
    @Bean
    public List<ICAPMessage> closeCommands() {
        List<ICAPMessage> msgs = new ArrayList<>();
        msgs.add(new ICAPMessage(new ICMsgOrderBookRemove()));
        msgs.add(new ICAPMessage(new ICMsgTradeBookRemove()));
        return msgs;
    }

    /**
     * @return
     */
    @Bean
    public ICAPConnector[] connectors() {
        return new ICAPConnector[]{
                new ICAPConnector(
                        ServerSignature.builder()
                                .ssl(ssl)
                                .host(host)
                                .port(port)
                                .key(key)
                                .value(value)
                                .proxyHost(proxyHost)
                                .proxyPort(proxyPort).build(),
                        icapMessageRepo, icapDispatchQueue, initCommands(), closeCommands(), serverStatus
                )
        };
    }

    /**
     * @return
     */
    @Bean
    public TradeVault tradeVault() {
        return new TradeVault();
    }
}