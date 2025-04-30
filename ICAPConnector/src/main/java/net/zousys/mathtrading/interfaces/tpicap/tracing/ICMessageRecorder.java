package net.zousys.mathtrading.interfaces.tpicap.tracing;

import com.icap.iConnect.srcMsgs.enums.EICErr;
import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.*;
import com.icap.iConnect.srcMsgs.vectors.MarketPermsVector;
import com.icap.iConnect.srcMsgs.vectors.MarketSubmarketPermsVector;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.tpicap.ICAPMessage;
import net.zousys.mathtrading.interfaces.tpicap.config.Constants;
import net.zousys.mathtrading.interfaces.tpicap.config.EssentialConfig;
import net.zousys.mathtrading.interfaces.tpicap.model.MsgClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 *
 */
@Slf4j
@Component
public class ICMessageRecorder extends Recorder {
    @Value("${app.tracing.path.raw}")
    private String icmsgTraceRoot;
    @Autowired
    private EssentialConfig.EnumConfig enumConfig;
    @Autowired
    private MsgClassifier classifier;
    @Autowired
    private ExecutorService recorderService;
    @Value("${app.tracing.message.detailed}")
    private boolean detailed;
    /**
     *
     * @param msg
     * @return
     */
    public static final boolean logMessage(ICAPMessage msg, boolean detailed, EssentialConfig.EnumConfig enumConfig, MsgClassifier classifier) {
        boolean bSuccess = true;
        if (enumConfig.getContentLevel() != Constants.ContentLevel.NONE) {
            EICMsgType msgType = msg.icMsg.getMsgType();
            if (classifier.isQualified(msgType.name())
                    ||enumConfig.getContentLevel()== Constants.ContentLevel.INBOUND
                    ||enumConfig.getContentLevel()== Constants.ContentLevel.OUTBOUND) {
                switch (msgType) {
                    case EICMsgType.eMsgHeartbeat -> {
                        break;
                    }
                    case EICMsgType.eMsgPositive -> {
                        log.info(MessageLogGenerator.generateLog(detailed, (ICMsgPositive) msg.icMsg, "Pos. Resp"));
                        break;
                    }
                    case EICMsgType.eMsgNegative -> {
                        log.info(MessageLogGenerator.generateLog(detailed, (ICMsgNegative) msg.icMsg, "Neg. Resp", ((ICMsgNegative) msg.icMsg).getDescription()));
                        break;
                    }
                    case EICMsgType.eMsgPositiveLogin -> {
                        doMsgPositiveLogin(msg.icMsg, detailed);
                        break;
                    }
                    case EICMsgType.eMsgMessageLogUpdate -> {
                        log.info(MessageLogGenerator.generateLog(detailed, (ICMsgLogUpdate) msg.icMsg, "LogUpdate", ((ICMsgLogUpdate) msg.icMsg).getMessage()));
                        break;
                    }
                    case EICMsgType.eMsgClearBook -> {
                        log.info(MessageLogGenerator.generateLog(detailed, (ICMsgClearBookUpdate) msg.icMsg, "ClearBook"));
                        break;
                    }

                    case EICMsgType.eMsgInvalid -> {
                        ICMsgUnknown unkmessage = (ICMsgUnknown) msg.icMsg;
                        if (EICErr.eErrMsgInvalid == unkmessage.getErrType()) {
                            StringBuffer sBuff = new StringBuffer();
                            sBuff.append("Invalid Msg received (MsgType: " + unkmessage.getOriginMsgType().getValue() + ")\n");
                            sBuff.append("API Version: " + unkmessage.getSoftwareVersion() + "\n");
                            sBuff.append("Desc: " + unkmessage.getDescription() + "\n");
                            log.info(MessageLogGenerator.generateLog(detailed, unkmessage, "Unknown", sBuff.toString()));
                        }
                        break;
                    }
                    default -> {
                        StringBuffer sBuff = new StringBuffer();
                        sBuff.append("\nMsg received (MsgType: " + msg.getType() + ")\n");
                        log.info(MessageLogGenerator.generateLog(detailed, msg.icMsg, sBuff.toString()));
                        bSuccess = false;
                    }
                }
            }
        }
        return bSuccess;
    }

    /**
     *
     * @param msg
     * @return
     */
    public boolean record(ICAPMessage msg) {
        recordMessage(msg, new File(icmsgTraceRoot).toPath(), recorderService);
        return logMessage(msg, detailed, enumConfig, classifier);
    }


    /**
     *
     * @param msgRef
     */
    private static final void doMsgPositiveLogin(ICMsg msgRef, boolean detailed) {
        ICMsgPositiveLogin iCMsg = (ICMsgPositiveLogin) msgRef;

        log.debug(MessageLogGenerator.generateLog(detailed, iCMsg, "Pos. Login (Successful Login)"));

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
