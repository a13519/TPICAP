package com.sc.race.asl;

import com.icap.iConnect.srcMsgs.iCMsg.*;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import net.zousys.mathtrading.interfaces.icap.tracing.RecordableMessage;
import net.zousys.mathtrading.interfaces.util.FileReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class RecordableMessageTest {
    @Test
    public void testSerialize() {
        ICMsg msg = new ICMsgLogUpdate();
        byte[] bytes = new RecordableMessage(msg).serialize();
        assert(bytes.length==201);
        assert(bytes[0]==64);
        ICMsgLogUpdate lu = (ICMsgLogUpdate)RecordableMessage.parse(bytes);
        assert(lu != null);

        msg = new ICMsgPositive();
        bytes = new RecordableMessage(msg).serialize();
        assert(bytes.length==201);
        assert(bytes[0]==102);
        ICMsgPositive p = (ICMsgPositive)RecordableMessage.parse(bytes);
        assert(p != null);

        msg = new ICMsgPositiveLogin();
        bytes = new RecordableMessage(msg).serialize();
        assert(bytes.length==201);
        assert(bytes[0]==105);
        ICMsgPositiveLogin mpl = (ICMsgPositiveLogin)RecordableMessage.parse(bytes);
        assert(mpl != null);

        msg = new ICMsgHeartbeat();
        bytes = new RecordableMessage(msg).serialize();
        assert(bytes.length==201);
        assert(bytes[0]==100);
        ICMsgHeartbeat hb = (ICMsgHeartbeat)RecordableMessage.parse(bytes);
        assert(hb != null);

        msg = new ICMsgClearBookUpdate();
        bytes = new RecordableMessage(msg).serialize();
        assert(bytes.length==201);
        assert(bytes[0]==91);
        ICMsgClearBookUpdate cbu = (ICMsgClearBookUpdate)RecordableMessage.parse(bytes);
        assert(cbu != null);
    }

}
