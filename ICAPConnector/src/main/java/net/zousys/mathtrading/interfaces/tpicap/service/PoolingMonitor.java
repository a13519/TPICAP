package net.zousys.mathtrading.interfaces.tpicap.service;

import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.tpicap.ICAPMessage;
import net.zousys.mathtrading.interfaces.tpicap.ParsingException;
import net.zousys.mathtrading.interfaces.tpicap.model.ICAPMessageRepo;
import net.zousys.mathtrading.interfaces.tpicap.ICAPSource;
import net.zousys.mathtrading.interfaces.util.FileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 *
 */
@Slf4j
@Component
public class PoolingMonitor {
    @Value("${app.pooling.active}")
    private boolean poolingActive;
    @Value("${app.pooling.path}")
    private String poolingPath;
    @Autowired
    private ExecutorService monitorService;
    @Autowired
    private ICAPSource icapSource;
    @Autowired
    private ICAPMessageRepo icapMessageRepo;

    /**
     *
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startMonitoring() {
        if (poolingActive) {
            CompletableFuture.runAsync(() -> {
                try {
                    monitorFolder();
                } catch (IOException | InterruptedException e) {
                    System.err.println("Error monitoring folder: " + e.getMessage());
                }
            }, monitorService);
        }
    }

    /**
     * @throws IOException
     * @throws InterruptedException
     */
    private void monitorFolder() throws IOException, InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path basePath = Paths.get(poolingPath);
        Set<Path> registeredPaths = new HashSet<>();

        registerDirectory(basePath, watchService, registeredPaths);
        log.info("Monitoring folder: " + basePath);

        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    log.warn("Event overflow occurred");
                    continue;
                }
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();
                Path fullPath = ((Path) key.watchable()).resolve(fileName);
                if (kind == StandardWatchEventKinds.ENTRY_CREATE
                        || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    if (Files.isRegularFile(fullPath)) {
                        log.info("New file detected: " + fullPath);
                        try {
                            icapMessageRepo.push(ICAPMessage.form(FileReader.readFileToBytes(fullPath)));
                        } catch (ParsingException e) {
                            log.warn(e.getLocalizedMessage()+": "+fullPath.getFileName());
                        }
                        Files.delete(fullPath);
                    } else if (Files.isDirectory(fullPath) && !registeredPaths.contains(fullPath)) {
                        log.warn("New folder detected: " + fullPath);
                        registerDirectory(fullPath, watchService, registeredPaths);
                    }
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                log.error("WatchKey no longer valid; exiting");
                break;
            }
        }
        watchService.close();
    }

    /**
     * @param dir
     * @param watchService
     * @param registeredPaths
     * @throws IOException
     */
    private void registerDirectory(Path dir, WatchService watchService, Set<Path> registeredPaths) throws IOException {
        dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        registeredPaths.add(dir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path subDir : stream) {
                if (Files.isDirectory(subDir) && !registeredPaths.contains(subDir)) {
                    registerDirectory(subDir, watchService, registeredPaths);
                }
            }
        }
    }
}