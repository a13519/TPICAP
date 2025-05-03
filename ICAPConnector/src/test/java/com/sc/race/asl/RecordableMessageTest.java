package com.sc.race.asl;

import com.icap.iConnect.srcMsgs.iCMsg.*;
import net.zousys.mathtrading.interfaces.tpicap.tracing.RecordableMessage;
import org.junit.jupiter.api.Test;

public class RecordableMessageTest {
    @Test
    public void testSerialize() {
        ICMsg msg = new ICMsgLogUpdate();
        byte[] bytes = new RecordableMessage(msg, System.currentTimeMillis()).serialize();
        assert (bytes.length == 201);
        assert (bytes[0] == 64);
        ICMsgLogUpdate lu = (ICMsgLogUpdate) RecordableMessage.parse(bytes).getIcMsg();
        assert (lu != null);
    }

    //    @Test
    public void testSerialize2() {
        ICMsg msg = new ICMsgPositive();
        byte[] bytes = new RecordableMessage(msg, System.currentTimeMillis()).serialize();
        assert (bytes.length == 201);
        assert (bytes[0] == 102);
        ICMsgPositive p = (ICMsgPositive) RecordableMessage.parse(bytes).getIcMsg();
        assert (p != null);
    }

    //    @Test
    public void testSerialize3() {
        ICMsg msg = new ICMsgPositiveLogin();
        byte[] bytes = new RecordableMessage(msg, System.currentTimeMillis()).serialize();
        assert (bytes.length == 201);
        assert (bytes[0] == 105);
        ICMsgPositiveLogin mpl = (ICMsgPositiveLogin) RecordableMessage.parse(bytes).getIcMsg();
        assert (mpl != null);
    }

    @Test
    public void testSerialize4() {
        ICMsg msg = new ICMsgHeartbeat();
        byte[] bytes = new RecordableMessage(msg, System.currentTimeMillis()).serialize();
        assert (bytes.length == 201);
        assert (bytes[0] == 100);
        ICMsgHeartbeat hb = (ICMsgHeartbeat) RecordableMessage.parse(bytes).getIcMsg();
        assert (hb != null);
    }

    @Test
    public void testSerialize5() {
        ICMsg msg = new ICMsgClearBookUpdate();
        byte[] bytes = new RecordableMessage(msg, System.currentTimeMillis()).serialize();
        assert (bytes.length == 201);
        assert (bytes[0] == 91);
        ICMsgClearBookUpdate cbu = (ICMsgClearBookUpdate) RecordableMessage.parse(bytes).getIcMsg();
        assert (cbu != null);
    }

}
