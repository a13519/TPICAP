package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.enums.EICErr;
import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.*;
import com.icap.iConnect.srcMsgs.vectors.MarketIdVector;
import com.icap.iConnect.srcMsgs.vectors.MarketPermsVector;
import com.icap.iConnect.srcMsgs.vectors.MarketSubmarketPermsVector;
import com.icap.iConnect.srcMsgs.vectors.SubmarketPermsVector;
import com.icap.iConnect.srcSession.ICSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private String icmsgTraceRoot;
    private int level;
    private MessageLogGenerator messageLogGenerator;
    private Path icmsgTraceRootPath;

    /**
     * Default Constructor. (Should not use)
     */
    @Autowired
    public ICMessageRecorder(
            @Value("${app.tracing.path.raw}")
            String icmsgTraceRoot,
            @Value("${app.tracing.message.level}")
            int level) {
        this.level = level;
        icmsgTraceRootPath = Paths.get(icmsgTraceRoot);
    }

    /**
     * Process messages from server.
     *
     * @param msg     ICMsg
     * @param session ICSession
     * @return <tt>true</tt> if success
     */
    public boolean record(ICMsg msg, ICSession session) {
        if (level > 0) {
            try {
                recordMessage(msg, icmsgTraceRootPath);
            } catch (IOException e) {
                log.error("ICMsg was not stored {}.{}", msg.getMsgType(), msg.getSequenceNumber());
                log.error("------------------");
                log.error(msg.toString());
                log.error("------------------");
            }
        }
        boolean bSuccess = true;
        if (level >= LOG_MESSAGES) {
            EICMsgType msgType = msg.getMsgType();
            switch (msgType) {
                case EICMsgType.eMsgHeartbeat -> {
                    break;
                }
                case EICMsgType.eMsgPositive -> {
                    log.debug(messageLogGenerator.generateLog((ICMsgPositive) msg, "Pos. Resp"));
                    break;
                }
                case EICMsgType.eMsgNegative -> {
                    log.info(messageLogGenerator.generateLog((ICMsgNegative) msg, "Neg. Resp", ((ICMsgNegative) msg).getDescription()));
                    break;
                }
                case EICMsgType.eMsgPositiveLogin -> {
                    doMsgPositiveLogin(msg);
                    break;
                }
                case EICMsgType.eMsgMessageLogUpdate -> {
                    log.debug(messageLogGenerator.generateLog((ICMsgLogUpdate) msg, "LogUpdate", ((ICMsgLogUpdate) msg).getMessage()));
                    break;
                }
                case EICMsgType.eMsgClearBook -> {
                    log.info(messageLogGenerator.generateLog((ICMsgClearBookUpdate) msg, "ClearBook"));
                    break;
                }
                case EICMsgType.eMsgInvalid -> {
                    ICMsgUnknown message = (ICMsgUnknown) msg;

                    if (EICErr.eErrMsgInvalid == message.getErrType()) {
                        StringBuffer sBuff = new StringBuffer();
                        sBuff.append("Invalid Msg received (MsgType: " + message.getOriginMsgType().getValue() + ")\n");
                        sBuff.append("API Version: " + message.getSoftwareVersion() + "\n");
                        sBuff.append("Desc: " + message.getDescription() + "\n");
                        log.info(messageLogGenerator.generateLog(message, "Unknown", sBuff.toString()));
                    }
                    break;
                }
                default -> {
                    StringBuffer sBuff = new StringBuffer();
                    sBuff.append("Msg received (MsgType: " + msg.getMsgType().getValue() + ")\n");
                    log.info(messageLogGenerator.generateLog(msg, sBuff.toString()));
                    bSuccess = false;
                }
            }
        }
        return bSuccess;
    }

    /**
     *
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
                // use Market view permission as an example
                if (permType == 1) {
                    ICMarketViewPerm ext = (ICMarketViewPerm) icMktPerm.getMarketPermsExtensionUnion().getExtension();
                    // Get the permission. 0 - not allowed, 1 - allowed.
                    byte perm = (byte) ext.getPerm();
                    log.info("Market: {} - Type: {} - PERM: {}",marketId, permType, perm==1?"ALLOW":"NOT ALLOW");
                }
            }

            // Submarket permission is the same as marketperms
            SubmarketPermsVector vecSubmktPerm = icMktSubmktPerms.getSubmarketPermsVector();
        }
    }
}
