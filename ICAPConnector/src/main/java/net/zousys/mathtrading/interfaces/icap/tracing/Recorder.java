package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgPositive;
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

    protected static final String messageId(long time, String type) {
        return time + "_" + type;
    }

    /**
     *
     * @param message
     * @param root
     * @param executorService
     */
    public static final void recordMessage(ICAPMessage message, Path root, ExecutorService executorService) {
        // TODO
//        CompletableFuture.runAsync(() -> {
            try {
                Path subroot = root.resolve(dateTag());
                Files.createDirectories(subroot);
                Path filepath = subroot.resolve(messageId(message.getTime(), message.getType())+".irm");
                Files.write(filepath, message.serialize()); // Write byte array to file
                filepath.toFile().setLastModified(message.getTime());
                log.info("Message was recorded: " + filepath);
            } catch (IOException e) {
                log.error("Record ICSMsg error: " + e.getLocalizedMessage());
                log.error("ICMsg was not stored {}.{}", message.getType(), message.getIcMsg().getSequenceNumber());
                log.error("------------------");
                log.error(message.toString());
                log.error("------------------");
            }
//        }, executorService);


    }


}
