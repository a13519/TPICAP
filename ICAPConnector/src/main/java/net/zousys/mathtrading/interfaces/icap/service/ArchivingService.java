package net.zousys.mathtrading.interfaces.icap.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@EnableScheduling
public class ArchivingService {
    @Value("${app.trace.path.success}")
    private String sucessPath;
    @Value("${app.trace.path.pending}")
    private String pendingPath;
    @Value("${app.trace.path.failure}")
    private String failurePath;
    private String archiverPath;
    private int threshold = 0;

    private File success = new File(sucessPath);
    private File pending = new File(pendingPath);
    private File failure = new File(failurePath);

    /**
     *
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void archiveAll() {
        try {
            archiveJSONMessageFiles(success);
            archiveJSONMessageFiles(pending);
            archiveJSONMessageFiles(failure);
        } catch (IOException e) {
            log.error("Archive caught exception: {}", e.getLocalizedMessage());
        }
    }
    /**
     * @param file
     * @throws IOException
     */
    public void archiveJSONMessageFiles(File file) throws IOException {
        // Generate archive file name with timestamp
        String archiveFileName = String.format(archiverPath, file.getName(), System.currentTimeMillis());
        Path archivePath = Paths.get(archiveFileName);

        // Ensure archive directory exists
        Files.createDirectories(archivePath.getParent());

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archiveFileName))) {
            Path basePath = file.toPath();
            Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.isRegularFile(file) && isOlderThanThreshold(attrs)) {
                        // Add file to ZIP
                        String entryName = basePath.relativize(file).toString();
                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(file, zos);
                        zos.closeEntry();
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        log.info("Archive created: " + archiveFileName);
    }

    /**
     * @param attrs
     * @return
     */
    private boolean isOlderThanThreshold(BasicFileAttributes attrs) {
        Instant lastModified = attrs.lastModifiedTime().toInstant();
        Instant thresholdi = Instant.now().minus(threshold, ChronoUnit.DAYS);
        return lastModified.isBefore(thresholdi);
    }

}
