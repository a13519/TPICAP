package net.zousys.mathtrading.interfaces.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReader {

    /**
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static final byte[] readFileToBytes(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            return bytes;
        }
    }

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    public static final byte[] readFileToBytes(String name) throws IOException {
        return readFileToBytes(Paths.get(name));
    }

    /**
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static final byte[] readFileToBytes(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        return is.readAllBytes();
    }
}
