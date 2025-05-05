package com.sc.race.asl;

import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.*;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.tpicap.ICAPMessage;
import net.zousys.mathtrading.interfaces.tpicap.ParsingException;
import net.zousys.mathtrading.interfaces.tpicap.tracing.RecordableMessage;
import net.zousys.mathtrading.interfaces.util.FileReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
@Slf4j
public class ICAPMessageTest {

    @Test
    public void testCBUFileData() throws IOException, ParsingException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("1745911350698_eMsgClearBook.irm"));
        ICMsgClearBookUpdate lu = (ICMsgClearBookUpdate) ICAPMessage.parse(bb).getIcMsg();
//        ICMsgClearBookUpdate lu = (ICMsgClearBookUpdate) RecordableMessage.parse(bb).getIcMsg();
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        log.info(sb.toString());
        assert (lu != null);
        assert (lu.getMsgType().equals(EICMsgType.eMsgClearBook));
        assert (lu.getFirmId().equals("000000"));
    }

    @Test
    public void testLUFileData() throws IOException, ParsingException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("1745911352475_eMsgMessageLogUpdate.irm"));
        ICMsgLogUpdate lu = (ICMsgLogUpdate) ICAPMessage.parse(bb).getIcMsg();
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        log.info(sb.toString());
        assert (lu != null);
        assert (lu.getMsgType().equals(EICMsgType.eMsgMessageLogUpdate));
        assert (lu.getFirmId().equals("000001"));
    }

    //    @Test
    public void testPFileData() throws IOException, ParsingException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("1745911353821_eMsgPositive.irm"));
        ICMsgPositive lu = (ICMsgPositive) ICAPMessage.parse(bb).getIcMsg();
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        log.info(sb.toString());
        assert (lu != null);
        assert (lu.getMsgType().equals(EICMsgType.eMsgPositive));
        assert (lu.getFirmId().equals("000001"));
        assert (lu.getConditionSubrecord().getConditionVector().size() == 0);
    }

    @Test
    public void testHBFileData() throws IOException, ParsingException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("1745911353148_eMsgHeartbeat.irm"));
        ICMsgHeartbeat lu = (ICMsgHeartbeat) ICAPMessage.parse(bb).getIcMsg();
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        log.info(sb.toString());
        assert (lu != null);
        assert (lu.getMsgType().equals(EICMsgType.eMsgHeartbeat));
        assert (lu.getFirmId().equals("000002"));
    }

    //    @Test
    public void testPLFileData() throws IOException, ParsingException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("1745911349680_eMsgPositiveLogin.irm"));
        ICMsgPositiveLogin lu = (ICMsgPositiveLogin) ICAPMessage.parse(bb).getIcMsg();
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        log.info(sb.toString());
        assert (lu != null);
        assert (lu.getMsgType().equals(EICMsgType.eMsgPositiveLogin));
        assert (lu.getFirmId().equals("000001"));
    }


    @Test
    public void testETData() throws IOException, ParsingException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("icap/1745939895420_eMsgElectronicTransaction.irm"));
        ICMsgElectronicTransaction lu = (ICMsgElectronicTransaction) ICAPMessage.parse(bb).getIcMsg();
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        log.info(sb.toString());
        assert (lu != null);
        assert (lu.getMsgType().equals(EICMsgType.eMsgElectronicTransaction));
        assert (lu.getFirmId().equals("000001"));
    }
}
