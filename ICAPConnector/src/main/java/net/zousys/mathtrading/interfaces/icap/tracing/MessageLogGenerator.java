package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.iCMsg.ICExtension;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
@Component
public class MessageLogGenerator {
    @Value("${app.tracing.message.detailed}")
    private boolean detailed;

    /**
     * @param msg
     * @param strName
     * @param strLog
     * @param strMsgType
     * @return
     */
    public String generateLog(ICMsg msg, String strName, String strLog, String strMsgType) {
        StringBuffer sBuff = new StringBuffer();
        if (strMsgType.length() == 0) {
            sBuff.append(String.format("%12s", " "));
        } else {
            sBuff.append(String.format("%-12s", strMsgType));
        }
        sBuff.append(strName);
        sBuff.append("\n");
        sBuff.append(strLog);

        if (detailed) {
            sBuff.append("\n");
            msg.dump(sBuff);
        }

        return sBuff.toString();
    }

    /**
     * @param msg
     * @param strName
     * @param strLog
     * @return
     */
    public String generateLog(ICMsg msg, String strName, String strLog) {
        return generateLog(msg, strName, strLog, "");
    }

    /**
     * @param msg
     * @param strName
     * @return
     */
    public String generateLog(ICMsg msg, String strName) {
        return generateLog(msg, strName, "", "");
    }

    /**
     * @param msg
     * @param strName
     * @param strLog
     * @return
     */
    public String generateRequestLog(ICMsg msg, String strName, String strLog) {
        if (msg != null) {
            StringBuffer sBuff = new StringBuffer();
            sBuff.append(" [" + msg.getMsgType().getValue() + "/" + msg.getRequestId() + "] ");
            return generateLog(msg, strName, strLog, sBuff.toString());
        }
        return "NULL";
    }

    /**
     *
     * @param msg
     * @param strName
     * @param strLog
     * @return
     */
    public String generateResponseLog(ICMsg msg, String strName, String strLog) {
        if (msg != null) {
            StringBuffer sBuff = new StringBuffer();

            // Include orig request type / request id to the name
            sBuff.append(" [" + ((ICMsgResponse) msg).getRequestType().getValue() + "/"
                    + msg.getRequestId() + "] ");

            // Log extension type
            return generateLog(msg, strName, strLog, sBuff.toString());
        }
        return "NULL";
    }

    /**
     * @param msg
     * @param strName
     * @return
     */
    public String generateResponseLog(ICMsg msg, String strName) {
        return generateResponseLog(msg, strName, "");
    }

    /**
     * @param extension
     * @param strName
     * @param strLog
     * @return
     */
    public String generateExtensionLog(ICExtension extension, String strName,
                                       String strLog) {
        StringBuffer sBuff = new StringBuffer();
        sBuff.append(strName);
        sBuff.append(" [" + extension.getExtensionType() + "]   ");
        sBuff.append("\n");
        sBuff.append(strLog);
        return sBuff.toString();
    }

}