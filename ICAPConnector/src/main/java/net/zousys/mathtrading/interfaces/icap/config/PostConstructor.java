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

    private File successFile = new File(success);
    private File failureFile = new File(failure);
    private File pendingFile = new File(pending);

    @PostConstruct
    public void construct() {
        if (!successFile.exists()) {
            successFile.mkdirs();
        }
        if (!failureFile.exists()) {
            failureFile.mkdirs();
        }
        if (!pendingFile.exists()) {
            pendingFile.mkdirs();
        }
    }
}
