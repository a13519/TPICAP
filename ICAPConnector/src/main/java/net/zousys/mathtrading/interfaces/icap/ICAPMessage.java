package net.zousys.mathtrading.interfaces.icap;

import com.icap.iConnect.srcMsgs.iCMsg.*;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import lombok.Getter;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.icap.tracing.RecordableMessage;
import net.zousys.mathtrading.interfaces.util.FileReader;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 */
public class ICAPMessage <T extends ICMsg> extends RecordableMessage implements Message {
    /**
     *
     * @param icMsg
     */
    public ICAPMessage(ICMsg icMsg) {
        super(icMsg, System.currentTimeMillis());
    }

    /**
     *
     * @param rMsg
     */
    public ICAPMessage(RecordableMessage rMsg) {
        super(rMsg.getIcMsg(), System.currentTimeMillis());
    }

    @Override
    public <T extends ICMsg> T message() {
        return (T) getIcMsg();
    }

    @Override
    public String getType() {
        return icMsg.getMsgType().name();
    }

    @Override
    public String getId() {
        return icMsg.getFirmId()+"."+icMsg.getRequestId()+"."+icMsg.getSequenceNumber() ;
    }

    /**
     * @param payload
     * @return
     */
    public static ICAPMessage form(byte[] payload) {
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
