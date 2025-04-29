package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgPositiveLogin;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.icap.ICAPMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public class Recorder {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    protected static final String dateTag() {
        return LocalDate.now().format(formatter);
    }

    protected static final String messageId(long time, ICMsg icMsg) {
        return time + "_" + icMsg.getMsgType();
    }

    /**
     * @param icMsg
     * @param root
     * @throws IOException
     */
    public static final void recordMessage(ICMsg icMsg, Path root, ExecutorService executorService) {
        CompletableFuture.runAsync(() -> {
            try {
                Path subroot = root.resolve(dateTag());
                Files.createDirectories(subroot);
                long time = System.currentTimeMillis();
                Path filepath = subroot.resolve(messageId(time, icMsg));

                EICMsgType type = icMsg.getMsgType();
                ICMessageBuffer icMessageBuffer = new ICMessageBuffer();
                ((ICMsgPositiveLogin)icMsg).pack(icMessageBuffer);
                byte[] b1 = icMessageBuffer.array();

                StringBuffer sb = new StringBuffer();
                icMsg.dump(sb);
                System.out.println(sb);

                ICMessageBuffer icMessageBuffer2 = new ICMessageBuffer();
                icMessageBuffer2.put(b1);
                icMessageBuffer2.flip();
                ICMsgPositiveLogin icMsg2 = new ICMsgPositiveLogin();
                icMsg2.unpack(icMessageBuffer2);

                StringBuffer sb2 = new StringBuffer();
                icMsg2.dump(sb2);
                System.out.println(sb2);



                Files.write(filepath, icMessageBuffer.array()); // Write byte array to file
                filepath.toFile().setLastModified(time);
                log.debug("Message was recorded: " + filepath);
            } catch (IOException e) {
                log.error("Record ICSMsg error: " + e.getLocalizedMessage());
                log.error("ICMsg was not stored {}.{}", icMsg.getMsgType(), icMsg.getSequenceNumber());
                log.error("------------------");
                log.error(icMsg.toString());
                log.error("------------------");
            }
        }, executorService);
    }
}
