package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.icap.ICAPSession;

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

    protected static final String messageId(ICMsg icMsg) {
        return System.currentTimeMillis() + "_" + icMsg.getMsgType();
    }

    /**
     *
     * @param icMsg
     * @param root
     * @throws IOException
     */
    protected static final void recordMessage(ICMsg icMsg, Path root) throws IOException {
        Path subroot = root.resolve(dateTag());
        Files.createDirectories(subroot);
        Path filepath = subroot.resolve(messageId(icMsg));
        ICMessageBuffer icMessageBuffer = new ICMessageBuffer();
        icMsg.pack(icMessageBuffer);
        Files.write(filepath, icMessageBuffer.array()); // Write byte array to file
        log.debug("Message was recorded: " + filepath);
    }
}
