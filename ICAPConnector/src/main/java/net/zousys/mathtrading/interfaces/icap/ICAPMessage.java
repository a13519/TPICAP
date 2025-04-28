package net.zousys.mathtrading.interfaces.icap;

import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import lombok.Getter;
import net.zousys.mathtrading.interfaces.Message;

/**
 *
 */
@Getter
public class ICAPMessage implements Message {
    private ICMsg icMsg;

    public ICAPMessage(ICMsg icMsg) {
        this.icMsg = icMsg;
    }

    @Override
    public <T extends ICMsg> ICMsg message() {
        return null;
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public String getId() {
        return "";
    }

    /**
     * @param playload
     * @return
     */
    public static Message form(byte[] playload) {
        if (playload != null) {
            ICMessageBuffer icMessageBuffer = new ICMessageBuffer();
            icMessageBuffer.put(playload);
            ICMsg icMsg = new ICMsg();
            icMsg.unpack(icMessageBuffer);
            return new ICAPMessage(icMsg);
        } else {
            return new ICAPMessage(new ICMsg());
        }
    }
}
