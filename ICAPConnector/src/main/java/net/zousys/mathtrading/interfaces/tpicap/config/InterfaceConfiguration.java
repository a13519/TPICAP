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
    public TradeVault tradeVault() {
        return new TradeVault();
    }
}