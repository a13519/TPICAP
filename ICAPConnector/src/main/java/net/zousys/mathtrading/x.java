import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
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
    private static final String ZIP_FILE = ROOT_DIR + "/archive_" + System.currentTimeMillis() + ".zip";
    private static final long DAYS_OLD = 10;

    @Scheduled(cron = "0 0 1 * * ?") // Runs daily at 1 AM
    public void zipOldFolders() {
        try {
            Path rootPath = Paths.get(ROOT_DIR);
            Instant cutoff = Instant.now().minus(DAYS_OLD, ChronoUnit.DAYS);

            // Create the zip file
            try (FileOutputStream fos = new FileOutputStream(ZIP_FILE);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // Find and process subdirectories
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath, Files::isDirectory)) {
                    for (Path folder : stream) {
                        if (Files.isSameFile(folder, rootPath)) {
                            continue; // Skip the root directory itself
                        }

                        BasicFileAttributes attrs = Files.readAttributes(folder, BasicFileAttributes.class);
                        if (attrs.lastModifiedTime().toInstant().isBefore(cutoff)) {
                            LOGGER.info("Zipping folder: {}", folder);
                            zipFolder(folder, zos);
                            LOGGER.info("Deleting folder: {}", folder);
                            deleteFolder(folder);
                        }
                    }
                }
                LOGGER.info("Zip file created: {}", ZIP_FILE);
            } catch (IOException e) {
                LOGGER.error("Error during zipping or deletion: {}", e.getMessage(), e);
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error: {}", e.getMessage(), e);
        }
    }

    private void zipFolder(Path folder, ZipOutputStream zos) throws IOException {
        Files.walk(folder)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        // Create zip entry with relative path
                        String zipEntryName = folder.getParent().relativize(path).toString();
                        ZipEntry zipEntry = new ZipEntry(zipEntryName);
                        zos.putNextEntry(zipEntry);

                        // Write file content to zip
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        LOGGER.error("Error zipping file {}: {}", path, e.getMessage(), e);
                    }
                });
    }

    private void deleteFolder(Path folder) throws IOException {
        Files.walk(folder)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
