package net.zousys.mathtrading.interfaces.icap.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class PostConstructor {
    @Value("${app.tracing.path.success}")
    private String success;
    @Value("${app.tracing.path.failure}")
    private String failure;
    @Value("${app.tracing.path.pending}")
    private String pending;
    @Value("${app.tracing.path.raw}")
    private String raw;
    @Value("${app.pooling.path}")
    private String pooling;
    @Value("${app.tracing.archiving.path}")
    private String archiverPath;
    @PostConstruct
    public void construct() {
        File successFile = new File(success);
        File failureFile = new File(failure);
        File pendingFile = new File(pending);
        File rawFile = new File(raw);
        File poolingFile = new File(pooling);
        File archiverFile = new File(archiverPath);
        if (!successFile.exists()) {
            successFile.mkdirs();
        }
        if (!failureFile.exists()) {
            failureFile.mkdirs();
        }
        if (!pendingFile.exists()) {
            pendingFile.mkdirs();
        }
        if (!rawFile.exists()) {
            rawFile.mkdirs();
        }
        if (!poolingFile.exists()) {
            poolingFile.mkdirs();
        }
        if (!archiverFile.exists()) {
            archiverFile.mkdirs();
        }
    }
}
