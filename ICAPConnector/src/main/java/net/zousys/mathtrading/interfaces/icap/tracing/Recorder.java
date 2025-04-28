package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.icap.ICAPSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Recorder {
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    protected static final String dateTag() {
        return LocalDate.now().format(formatter);
    }

    protected static final String messageId(long time, ICMsg icMsg) {
        return time + "_" + icMsg.getMsgType();
    }

    /**
     *
     * @param icMsg
     * @param root
     * @throws IOException
     */
    public static final void recordMessage(ICMsg icMsg, Path root) throws IOException {
        Path subroot = root.resolve(dateTag());
        Files.createDirectories(subroot);
        long time = System.currentTimeMillis();
        Path filepath = subroot.resolve(messageId(time, icMsg));
        ICMessageBuffer icMessageBuffer = new ICMessageBuffer();
        icMsg.pack(icMessageBuffer);
        Files.write(filepath, icMessageBuffer.array()); // Write byte array to file
        filepath.toFile().setLastModified(time);
        log.debug("Message was recorded: " + filepath);
    }
}
