import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FolderZipper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderZipper.class);
    private static final String ROOT_DIR = "/path/to/root/directory"; // Replace with your directory path
    private static final String OUTPUT_ZIP = "/path/to/output/archive.zip"; // Replace with desired zip file path
    private static final long DAYS_OLD = 10;

    @Scheduled(cron = "0 0 1 * * ?") // Runs daily at 1 AM
    public void zipOldFolders() {
        try {
            Path rootPath = Paths.get(ROOT_DIR);
            Instant cutoff = Instant.now().minus(DAYS_OLD, ChronoUnit.DAYS);

            // Create or overwrite the zip file
            try (FileOutputStream fos = new FileOutputStream(OUTPUT_ZIP);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // Find subdirectories older than 10 days
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath, Files::isDirectory)) {
                    for (Path folder : stream) {
                        BasicFileAttributes attrs = Files.readAttributes(folder, BasicFileAttributes.class);
                        if (attrs.lastModifiedTime().toInstant().isBefore(cutoff)) {
                            LOGGER.info("Zipping folder: {}", folder.getFileName());
                            zipFolder(folder, zos);
                            // Optionally delete the folder
                            Files.walk(folder)
                                    .sorted(Comparator.reverseOrder())
                                    .map(Path::toFile)
                                    .forEach(file -> {
                                        if (file.delete()) {
                                            LOGGER.debug("Deleted: {}", file.getPath());
                                        } else {
                                            LOGGER.warn("Failed to delete: {}", file.getPath());
                                        }
                                    });
                        }
                    }
                }
                LOGGER.info("Zip file created at: {}", OUTPUT_ZIP);
            }
        } catch (IOException e) {
            LOGGER.error("Error zipping folders: {}", e.getMessage(), e);
        }
    }

    private void zipFolder(Path folder, ZipOutputStream zos) throws IOException {
        Files.walk(folder)
                .forEach(path -> {
                    try {
                        // Skip directories themselves, only process files
                        if (!Files.isDirectory(path)) {
                            // Create zip entry relative to the root directory
                            String zipEntryName = folder.getParent().relativize(path).toString();
                            ZipEntry zipEntry = new ZipEntry(zipEntryName);
                            zos.putNextEntry(zipEntry);

                            // Write file content to zip
                            Files.copy(path, zos);
                            zos.closeEntry();
                            LOGGER.debug("Added to zip: {}", zipEntryName);
                        }
                    } catch (IOException e) {
                        LOGGER.error("Error adding {} to zip: {}", path, e.getMessage(), e);
                    }
                });
    }
}
