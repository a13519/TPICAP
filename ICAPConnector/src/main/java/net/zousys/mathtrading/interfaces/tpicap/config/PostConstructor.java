package net.zousys.mathtrading.interfaces.tpicap.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.tpicap.model.TradeVault;
import net.zousys.mathtrading.interfaces.tpicap.repository.TradeVaultRepository;
import net.zousys.mathtrading.interfaces.tpicap.service.TradeVaultService;
import net.zousys.mathtrading.interfaces.tpicap.tracing.Recorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
@Slf4j
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
    @Autowired
    private TradeVaultRepository tradeVaultRepository;
    @Autowired
    private TradeVault tradeVault;
    @Autowired
    private ZoneId zoneId;
    @Autowired
    private TradeVaultService tradeVaultService;

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
        Path subroot = new File(raw).toPath().resolve(Recorder.dateTag());
        try {
            Files.createDirectories(subroot);
        } catch (IOException e) {
            log.error("Failed to create directory: {}", subroot);
        }
        tradeVaultService.reloadTradeVault();
    }

}
