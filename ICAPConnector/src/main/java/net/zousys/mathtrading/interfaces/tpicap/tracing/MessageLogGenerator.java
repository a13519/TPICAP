package net.zousys.mathtrading.interfaces.tpicap.tracing;

import com.icap.iConnect.srcMsgs.iCMsg.ICExtension;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 */
public class MessageLogGenerator {
    /**
     *
     * @param detailed
     * @param msg
     * @param strName
     * @param strLog
     * @param strMsgType
     * @return
     */
    public static final String generateLog(boolean detailed, ICMsg msg, String strName, String strLog, String strMsgType) {
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
     *
     * @param detailed
     * @param msg
     * @param strName
     * @param strLog
     * @return
     */
    public static final String generateLog(boolean detailed, ICMsg msg, String strName, String strLog) {
        return generateLog(detailed, msg, strName, strLog, "");
    }

    /**
     *
     * @param detailed
     * @param msg
     * @param strName
     * @return
     */
    public static final String generateLog(boolean detailed, ICMsg msg, String strName) {
        return generateLog(detailed, msg, strName, "", "");
    }

    /**
     *
     * @param detailed
     * @param msg
     * @param strName
     * @param strLog
     * @return
     */
    public static final String generateRequestLog(boolean detailed, ICMsg msg, String strName, String strLog) {
        if (msg != null) {
            StringBuffer sBuff = new StringBuffer();
            sBuff.append(" [" + msg.getMsgType().getValue() + "/" + msg.getRequestId() + "] ");
            return generateLog(detailed, msg, strName, strLog, sBuff.toString());
        }
        return "NULL";
    }

    /**
     *
     * @param detailed
     * @param msg
     * @param strName
     * @param strLog
     * @return
     */
    public static final String generateResponseLog(boolean detailed, ICMsg msg, String strName, String strLog) {
        if (msg != null) {
            StringBuffer sBuff = new StringBuffer();

            // Include orig request type / request id to the name
            sBuff.append(" [" + ((ICMsgResponse) msg).getRequestType().getValue() + "/"
                    + msg.getRequestId() + "] ");

            // Log extension type
            return generateLog(detailed, msg, strName, strLog, sBuff.toString());
        }
        return "NULL";
    }

    /**
     *
     * @param detailed
     * @param msg
     * @param strName
     * @return
     */
    public static final String generateResponseLog(boolean detailed, ICMsg msg, String strName) {
        return generateResponseLog(detailed, msg, strName, "");
    }

    /**
     *
     * @param detailed
     * @param extension
     * @param strName
     * @param strLog
     * @return
     */
    public static final String generateExtensionLog(boolean detailed, ICExtension extension, String strName,
                                       String strLog) {
        StringBuffer sBuff = new StringBuffer();
        sBuff.append(strName);
        sBuff.append(" [" + extension.getExtensionType() + "]   ");
        sBuff.append("\n");
        sBuff.append(strLog);
        return sBuff.toString();
    }

}