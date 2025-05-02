package net.zousys.mathtrading.interfaces.tpicap.service;

import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.tpicap.ICAPSource;
import net.zousys.mathtrading.interfaces.tpicap.model.ServerStatus;
import net.zousys.mathtrading.interfaces.tpicap.model.TradeVault;
import net.zousys.mathtrading.interfaces.tpicap.repository.TradeVaultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

@Slf4j
@Service
public class PTFService {
    private boolean readyheartChecking = false;
    @Autowired
    private ICAPSource icapSource;
    @Autowired
    private ServerStatus serverStatus;
    @Value("${app.tracing.archiving.age}")
    private int ageInDays;
    @Value("${app.tracing.path.success}")
    private String success;
    @Value("${app.tracing.path.failure}")
    private String failure;
    @Value("${app.tracing.path.pending}")
    private String pending;

    /**
     *
     */
    public void startPTFInterface() {
        icapSource.startDeamon();
        readyheartChecking = true;
    }

    /**
     *
     */
    @Scheduled(fixedRateString = "${app.session.refresh:240000}")
    private void sessionChecking() {
        if (readyheartChecking) {
            icapSource.checkSession();
        }
    }

    /**
     *
     */
    @Scheduled(fixedRateString = "${app.session.status:240000}")
    private void serverStatus() {
        log.info("Regular status >> "+serverStatus.toString());
    }

    /**
     *
     */
    @Scheduled(fixedRateString = "${app.trace.archiving.interval:86400000}")
    private void archiving() {
        long age = ageInDays * 24 * 60 * 60 * 1000L;

        File successFile = new File(success);
        File failureFile = new File(failure);
        File pendingFile = new File(pending);

        archiveFile(successFile, age);
        archiveFile(failureFile, age);
        archiveFile(pendingFile, age);
    }

    /**
     * @param path
     * @param age
     */
    private static final void archiveFile(File path, long age) {
        long current = System.currentTimeMillis();
        Arrays.stream(path.listFiles()).toList().forEach(file -> {
            if (current - file.lastModified() > age) {
                if (!file.delete()) {
                    log.warn("File {} is not deleted", file.getAbsolutePath());
                }
            }
        });
    }


}
