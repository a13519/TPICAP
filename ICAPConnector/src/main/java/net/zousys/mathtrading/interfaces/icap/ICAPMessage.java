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
public class ICAPMessage extends RecordableMessage implements Message {

    public ICAPMessage(ICMsg icMsg) {
        super(icMsg);
    }

    @Override
    public <T extends ICMsg> ICMsg message() {
        return null;
    }

    @Override
    public String getType() {
        return icMsg.getMsgType().name();
    }

    @Override
    public String getId() {
        return icMsg.getFirmId();
    }

    /**
     * @param payload
     * @return
     */
    public static ICAPMessage form(byte[] payload) {
        if (payload != null) {
            ICMsg msg = RecordableMessage.parse(payload);
            return new ICAPMessage(msg);
        } else {
            return new ICAPMessage(new ICMsg());
        }
    }

}
