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
    public static final String IMS = ".ims";
    public static final String IRM = ".irm";
    @Value("${app.tracing.path.raw}")
    private String icmsgTraceRoot;
    @Value("${app.tracing.scripting}")
    private Boolean scripting;
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
     * @param content
     * @param type
     * @param time
     * @param executorService
     * @return
     */
    public final void logMessage(String content, String type, long time, ExecutorService executorService) {
        log.info(content);
        if (scripting && executorService != null) {
            recordMessage(content.getBytes(), IMS, type, time, new File(icmsgTraceRoot).toPath(), executorService);
        }
    }

    /**
     *
     * @param msg
     * @param detailed
     * @param enumConfig
     * @param classifier
     * @param executorService
     * @return
     */
    public final boolean logMessage(ICAPMessage msg, boolean detailed, EssentialConfig.EnumConfig enumConfig, MsgClassifier classifier, ExecutorService executorService) {
        boolean bSuccess = true;
        if (enumConfig.getContentLevel() != Constants.ContentLevel.NONE) {
            EICMsgType msgType = msg.icMsg.getMsgType();
            if (classifier.isQualified(msgType.name())
                    || enumConfig.getContentLevel() == Constants.ContentLevel.INBOUND
                    || enumConfig.getContentLevel() == Constants.ContentLevel.OUTBOUND) {
                switch (msgType) {
                    case EICMsgType.eMsgHeartbeat -> {
                        break;
                    }
                    case EICMsgType.eMsgPositive -> {
                        logMessage(MessageLogGenerator.generateLog(detailed, (ICMsgPositive) msg.icMsg, "Pos. Resp"), msg.getType(), msg.getTime(), executorService);
                        break;
                    }
                    case EICMsgType.eMsgNegative -> {
                        logMessage(MessageLogGenerator.generateLog(detailed, (ICMsgNegative) msg.icMsg, "Neg. Resp", ((ICMsgNegative) msg.icMsg).getDescription()), msg.getType(), msg.getTime(), executorService);
                        break;
                    }
                    case EICMsgType.eMsgPositiveLogin -> {
                        doMsgPositiveLogin(msg.icMsg, detailed);
                        break;
                    }
                    case EICMsgType.eMsgMessageLogUpdate -> {
                        logMessage(MessageLogGenerator.generateLog(detailed, (ICMsgLogUpdate) msg.icMsg, "LogUpdate", ((ICMsgLogUpdate) msg.icMsg).getMessage()), msg.getType(), msg.getTime(), executorService);
                        break;
                    }
                    case EICMsgType.eMsgClearBook -> {
                        logMessage(MessageLogGenerator.generateLog(detailed, (ICMsgClearBookUpdate) msg.icMsg, "ClearBook"), msg.getType(), msg.getTime(), executorService);
                        break;
                    }

                    case EICMsgType.eMsgInvalid -> {
                        ICMsgUnknown unkmessage = (ICMsgUnknown) msg.icMsg;
                        if (EICErr.eErrMsgInvalid == unkmessage.getErrType()) {
                            StringBuffer sBuff = new StringBuffer();
                            sBuff.append("Invalid Msg received (MsgType: " + unkmessage.getOriginMsgType().getValue() + ")\n");
                            sBuff.append("API Version: " + unkmessage.getSoftwareVersion() + "\n");
                            sBuff.append("Desc: " + unkmessage.getDescription() + "\n");
                            logMessage(MessageLogGenerator.generateLog(detailed, unkmessage, "Unknown", sBuff.toString()), msg.getType(), msg.getTime(), executorService);
                        }
                        break;
                    }
                    default -> {
                        StringBuffer sBuff = new StringBuffer();
                        sBuff.append("\nMsg received (MsgType: " + msg.getType() + ")\n");
                        logMessage(MessageLogGenerator.generateLog(detailed, msg.icMsg, sBuff.toString()), msg.getType(), msg.getTime(), executorService);
                        bSuccess = false;
                    }
                }
            }
        }
        return bSuccess;
    }

    /**
     * @param msg
     * @return
     */
    public boolean record(ICAPMessage msg) {
        recordMessage(msg, IRM, new File(icmsgTraceRoot).toPath(), recorderService);
        return logMessage(msg, detailed, enumConfig, classifier, recorderService);
    }


    /**
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
