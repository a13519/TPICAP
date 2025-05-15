package net.zousys.mathtrading.interfaces.tpicap.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
public class ArchivingService {
    @Value("${app.tracing.path.success}")
    private String sucessPath;
    @Value("${app.tracing.path.pending}")
    private String pendingPath;
    @Value("${app.tracing.path.failure}")
    private String failurePath;
    @Value("${app.tracing.path.raw}")
    private String rawPath;
    @Value("${app.tracing.archiving.path}")
    private String archiverPath;
    private int threshold = 0;


    /**
     *
     */
//    @Scheduled(cron = "0 0 2 * * ?")
    @EventListener(ApplicationReadyEvent.class)
    public void archiveAll() {
        File success = new File(sucessPath);
        File pending = new File(pendingPath);
        File failure = new File(failurePath);
        File raw = new File(rawPath);
        try {
            archiveMessageFiles(success);
            archiveMessageFiles(pending);
            archiveMessageFiles(failure);
            archiveMessageFiles(raw);
        } catch (IOException e) {
            log.error("Archive caught exception: {}", e.getLocalizedMessage());
        }
    }

    /**
     * @param file
     * @throws IOException
     */
    public void archiveMessageFiles(File file) throws IOException {
        File archiveFile = new File(archiverPath, file.getName()+"_"+System.currentTimeMillis()+".zip");
        Path archivePath = Paths.get(archiveFile.getAbsolutePath());
        Files.createDirectories(archivePath.getParent());

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archiveFile))) {
            Path basePath = file.toPath();
            Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.isRegularFile(file) && isOlderThanThreshold(attrs)) {
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
        log.info("Archive created: " + archiveFile);
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
