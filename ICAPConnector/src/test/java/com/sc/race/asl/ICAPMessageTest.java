package com.sc.race.asl;

import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.*;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import net.zousys.mathtrading.interfaces.icap.ICAPMessage;
import net.zousys.mathtrading.interfaces.icap.tracing.RecordableMessage;
import net.zousys.mathtrading.interfaces.util.FileReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ICAPMessageTest {

    @Test
    public void testCBUFileData() throws IOException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("1745911350698_eMsgClearBook.irm"));
        ICMsgClearBookUpdate lu = ICAPMessage.parse(bb);
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        System.out.println(sb);
        assert(lu!=null);
        assert(lu.getMsgType().equals(EICMsgType.eMsgClearBook));
        assert(lu.getFirmId().equals("000001"));
    }
    @Test
    public void testLUFileData() throws IOException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("1745911352475_eMsgMessageLogUpdate.irm"));
        ICMsgLogUpdate lu = ICAPMessage.parse(bb);
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        System.out.println(sb);
        assert(lu!=null);
        assert(lu.getMsgType().equals(EICMsgType.eMsgMessageLogUpdate));
        assert(lu.getFirmId().equals("000001"));
    }
    @Test
    public void testPFileData() throws IOException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("1745911353821_eMsgPositive.irm"));
        ICMsgPositive lu = ICAPMessage.parse(bb);
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        System.out.println(sb);
        assert(lu!=null);
        assert(lu.getMsgType().equals(EICMsgType.eMsgPositive));
        assert(lu.getFirmId().equals("000001"));
        assert(lu.getConditionSubrecord().getConditionVector().size()==0);
    }
    @Test
    public void testHBFileData() throws IOException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("1745911353148_eMsgHeartbeat.irm"));
        ICMsgHeartbeat lu = ICAPMessage.parse(bb);
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        System.out.println(sb);
        assert(lu!=null);
        assert(lu.getMsgType().equals(EICMsgType.eMsgHeartbeat));
        assert(lu.getFirmId().equals("000001"));
    }
}
