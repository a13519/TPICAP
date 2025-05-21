import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOldFolders {
    public static void main(String[] args) {
        String rootDirPath = "path/to/root/directory"; // Replace with your root directory path
        long daysThreshold = 10;

        try {
            zipFoldersOlderThan(rootDirPath, daysThreshold);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void zipFoldersOlderThan(String rootDirPath, long daysThreshold) throws Exception {
        Path rootPath = Paths.get(rootDirPath);
        Instant threshold = Instant.now().minus(daysThreshold, ChronoUnit.DAYS);

        // Walk through the root directory (one level deep for subfolders)
        Files.list(rootPath)
             .filter(Files::isDirectory) // Only directories
             .filter(path -> {
                 try {
                     FileTime lastModified = Files.getLastModifiedTime(path);
                     return lastModified.toInstant().isBefore(threshold);
                 } catch (Exception e) {
                     e.printStackTrace();
                     return false;
                 }
             })
             .forEach(path -> {
                 try {
                     zipFolder(path, path.getFileName() + ".zip");
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             });
    }

    public static void zipFolder(Path folderPath, String zipFileName) throws Exception {
        File zipFile = new File(folderPath.getParent().toString(), zipFileName);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.walk(folderPath)
                 .filter(path -> !Files.isDirectory(path)) // Only files, not directories
                 .forEach(path -> {
                     try {
                         // Create zip entry with relative path
                         String relativePath = folderPath.getParent().relativize(path).toString();
                         ZipEntry zipEntry = new ZipEntry(relativePath);
                         zos.putNextEntry(zipEntry);

                         // Write file content to zip
                         try (FileInputStream fis = new FileInputStream(path.toFile())) {
                             byte[] buffer = new byte[1024];
                             int len;
                             while ((len = fis.read(buffer)) > 0) {
                                 zos.write(buffer, 0, len);
                             }
                         }
                         zos.closeEntry();
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 });
        }
        System.out.println("Created zip: " + zipFile.getAbsolutePath());
    }
}
