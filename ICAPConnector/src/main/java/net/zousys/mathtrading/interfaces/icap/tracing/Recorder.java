package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import lombok.extern.slf4j.Slf4j;

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
                ICMessageBuffer icMessageBuffer = new ICMessageBuffer();
                icMsg.pack(icMessageBuffer);
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
