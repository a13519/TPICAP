package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.enums.EICErr;
import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.*;
import com.icap.iConnect.srcMsgs.vectors.MarketPermsVector;
import com.icap.iConnect.srcMsgs.vectors.MarketSubmarketPermsVector;
import com.icap.iConnect.srcMsgs.vectors.SubmarketPermsVector;
import com.icap.iConnect.srcSession.ICSession;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.icap.ICAPMessage;
import net.zousys.mathtrading.interfaces.icap.config.Constants;
import net.zousys.mathtrading.interfaces.icap.config.EssentialConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 *
 */
@Slf4j
@Component
public class ICMessageRecorder extends Recorder {
    public static final int NOTHING = 0;
    public static final int ALL_MESSAGES = 1;
    public static final int LOG_MESSAGES = 2;
    public static final int LOG_OUTBOUND = 3;
    @Value("${app.tracing.path.raw}")
    private String icmsgTraceRoot;
    @Autowired
    private EssentialConfig.EnumConfig enumConfig;
    @Autowired
    private ExecutorService recorderService;
    @Autowired
    private MessageLogGenerator messageLogGenerator;
    @Autowired
    private Set<String> bizTypes;

    /**
     * Process messages from server.
     *
     * @param msg ICMsg
     * @return <tt>true</tt> if success
     */
    public boolean record(ICAPMessage msg) {
        if (isSerialiable(msg)) {
            recordMessage(msg, new File(icmsgTraceRoot).toPath(), recorderService);
        }
        boolean bSuccess = true;
        if (enumConfig.getContentLevel() != Constants.ContentLevel.NONE) {
            EICMsgType msgType = msg.icMsg.getMsgType();
            if (bizTypes.contains(msgType.name())
                    ||enumConfig.getContentLevel()== Constants.ContentLevel.INBOUND
                    ||enumConfig.getContentLevel()== Constants.ContentLevel.OUTBOUND) {
                switch (msgType) {
                    case EICMsgType.eMsgHeartbeat -> {
                        break;
                    }
                    case EICMsgType.eMsgPositive -> {
                        log.info(messageLogGenerator.generateLog((ICMsgPositive) msg.icMsg, "Pos. Resp"));
                        break;
                    }
                    case EICMsgType.eMsgNegative -> {
                        log.info(messageLogGenerator.generateLog((ICMsgNegative) msg.icMsg, "Neg. Resp", ((ICMsgNegative) msg.icMsg).getDescription()));
                        break;
                    }
                    case EICMsgType.eMsgPositiveLogin -> {
                        doMsgPositiveLogin(msg.icMsg);
                        break;
                    }
                    case EICMsgType.eMsgMessageLogUpdate -> {
                        log.info(messageLogGenerator.generateLog((ICMsgLogUpdate) msg.icMsg, "LogUpdate", ((ICMsgLogUpdate) msg.icMsg).getMessage()));
                        break;
                    }
                    case EICMsgType.eMsgClearBook -> {
                        log.info(messageLogGenerator.generateLog((ICMsgClearBookUpdate) msg.icMsg, "ClearBook"));
                        break;
                    }

                    case EICMsgType.eMsgInvalid -> {
                        ICMsgUnknown unkmessage = (ICMsgUnknown) msg.icMsg;
                        if (EICErr.eErrMsgInvalid == unkmessage.getErrType()) {
                            StringBuffer sBuff = new StringBuffer();
                            sBuff.append("Invalid Msg received (MsgType: " + unkmessage.getOriginMsgType().getValue() + ")\n");
                            sBuff.append("API Version: " + unkmessage.getSoftwareVersion() + "\n");
                            sBuff.append("Desc: " + unkmessage.getDescription() + "\n");
                            log.info(messageLogGenerator.generateLog(unkmessage, "Unknown", sBuff.toString()));
                        }
                        break;
                    }
                    default -> {
                        StringBuffer sBuff = new StringBuffer();
                        sBuff.append("Msg received (MsgType: " + msg.getType() + ")\n");
                        log.info(messageLogGenerator.generateLog(msg.icMsg, sBuff.toString()));
                        bSuccess = false;
                    }
                }
            }
        }
        return bSuccess;
    }

    /**
     * @param icapMessage
     * @return
     */
    private boolean isSerialiable(ICAPMessage icapMessage) {
        if (enumConfig.getSerializeLevel() == Constants.SerializeLevel.ALL) {
            return true;
        } else {
            return bizTypes.contains(icapMessage.getType());
        }
    }

    /**
     * @param msgRef
     */
    private void doMsgPositiveLogin(ICMsg msgRef) {
        ICMsgPositiveLogin iCMsg = (ICMsgPositiveLogin) msgRef;

        log.debug(messageLogGenerator.generateLog(iCMsg, "Pos. Login (Successful Login)"));

        MarketSubmarketPermsVector vecMarketSubmarketPerms = iCMsg.getMarketSubmarketPermsRec().getMarketSubmarketPermsVector();
        for (ICMarketSubmarketPerms icMktSubmktPerms : vecMarketSubmarketPerms) {
            int marketId = icMktSubmktPerms.getMarketId().getValue();
            MarketPermsVector vecMktPerm = icMktSubmktPerms.getMarketPermsVector();

            for (ICMarketPerms icMktPerm : vecMktPerm) {
                int permType = icMktPerm.getSubType();
                if (permType == 1) {
                    ICMarketViewPerm ext = (ICMarketViewPerm) icMktPerm.getMarketPermsExtensionUnion().getExtension();
                    byte perm = (byte) ext.getPerm();
                    log.info("Market: {} - Type: {} - PERM: {}", marketId, permType, perm == 1 ? "ALLOW" : "NOT ALLOW");
                }
            }
        }
    }
}
