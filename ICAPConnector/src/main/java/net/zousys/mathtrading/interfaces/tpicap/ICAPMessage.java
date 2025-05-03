package net.zousys.mathtrading.interfaces.tpicap;

import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.tpicap.tracing.RecordableMessage;

/**
 *
 */
public class ICAPMessage<T> extends RecordableMessage implements Message {
    /**
     * @param icMsg
     */
    public ICAPMessage(ICMsg icMsg) {
        super(icMsg, System.currentTimeMillis());
    }

    /**
     * @param rMsg
     */
    public ICAPMessage(RecordableMessage rMsg) {
        super(rMsg.getIcMsg(), System.currentTimeMillis());
    }

    @Override
    public <T> T message() {
        return (T) getIcMsg();
    }

    /**
     * @return
     */
    public EICMsgType getICType() {
        return icMsg.getMsgType();
    }

    @Override
    public String getType() {
        return icMsg.getMsgType().name();
    }

    @Override
    public String getId() {
        return String.join(".", getType(), icMsg.getFirmId(), "" + icMsg.getRequestId(), "" + icMsg.getSequenceNumber());
    }

    /**
     * @param payload
     * @return
     */
    public static ICAPMessage form(byte[] payload) throws ParsingException {
        if (payload != null) {
            RecordableMessage msg = RecordableMessage.parse(payload);
            return new ICAPMessage(msg);
        } else {
            return new ICAPMessage(new ICMsg());
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        icMsg.dump(sb);
        return sb.toString();
    }
}
