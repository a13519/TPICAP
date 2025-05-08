package net.zousys.mathtrading.interfaces.tpicap.tracing;

import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.tpicap.ICAPMessage;

import java.io.IOException;
import java.io.InputStream;
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
     * @param message
     * @param root
     * @param executorService
     */
    public static final void recordMessage(ICAPMessage message, Path root, ExecutorService executorService) {
        recordMessage(message.serialize(), message.getType(), message.getTime(), root, executorService);
    }

    /**
     *
     * @param is
     * @param type
     * @param time
     * @param root
     * @param executorService
     */
    public static final void recordMessage(InputStream is, String type, long time, Path root, ExecutorService executorService) {
        try {
            recordMessage(is.readAllBytes(), type, time, root, executorService);
        } catch (IOException e) {
            log.error("InputStream readAllBytes error: " + e.getLocalizedMessage());
        }
    }
    /**
     *
     * @param data
     * @param type
     * @param time
     * @param root
     * @param executorService
     */
    public static final void recordMessage(byte[] data, String type, long time, Path root, ExecutorService executorService) {
        CompletableFuture.runAsync(() -> {
            try {
                Path subroot = root.resolve(dateTag());
                Files.createDirectories(subroot);
                Path filepath = subroot.resolve(messageId(time, type) + ".irm");
                Files.write(filepath, data); // Write byte array to file
                filepath.toFile().setLastModified(time);
                log.info("Message was recorded: " + filepath);
            } catch (IOException e) {
                log.error("Record ICSMsg error: " + e.getLocalizedMessage());
                log.error("ICMsg was not stored {}.{}", type, time);
                log.error("------------------");
                log.error(new String(data));
                log.error("------------------");
            }
        }, executorService);
    }
}
