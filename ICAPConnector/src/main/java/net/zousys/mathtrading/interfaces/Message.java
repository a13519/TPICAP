package net.zousys.mathtrading.interfaces;

import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;

public interface Message {
    public <T> T message();

    public String getType();

    public String getId();
}
