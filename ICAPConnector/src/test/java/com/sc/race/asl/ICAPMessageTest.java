package com.sc.race.asl;

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
    public void testFileData() throws IOException {
        byte[] bb = FileReader.readFileToBytes(getClass().getClassLoader().getResourceAsStream("1745840799784_eMsgMessageLogUpdate"));

        ICMsgLogUpdate lu = ICAPMessage.parse(bb);
        StringBuffer sb = new StringBuffer();
        lu.dump(sb);
        System.out.println(sb);
        assert(lu!=null);


    }
}
