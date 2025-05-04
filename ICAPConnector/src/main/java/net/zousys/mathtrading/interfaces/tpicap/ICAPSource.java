package net.zousys.mathtrading.interfaces.tpicap;


import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.SessionException;
import net.zousys.mathtrading.interfaces.Source;
import net.zousys.mathtrading.interfaces.tpicap.model.ICAPMessageRepo;
import net.zousys.mathtrading.interfaces.tpicap.model.ServerStatus;
import net.zousys.mathtrading.interfaces.tpicap.service.TradeVaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

/**
 *
 */
@Slf4j
@Component
public class ICAPSource implements Source {
    @Autowired
    private ServerStatus serverStatus;
    @Autowired
    private ICAPMessageRepo icapMessageRepo;
    @Autowired
    private TradeVaultService tradeVaultService;
    private ExecutorService processorService;
    private Flow.Subscriber<Message> subscriber;
    private ICAPConnector[] connectors;

    /**
     * @param connectors
     * @param collectorService
     * @param processorService
     * @param subscriber
     */
    @Autowired
    public ICAPSource(
            ICAPConnector[] connectors,
            ExecutorService processorService,
            Flow.Subscriber<Message> subscriber) {
        this.connectors = connectors;
        this.processorService = processorService;
        this.subscriber = subscriber;
    }

    /**
     *
     */
    @Override
    public void startDeamon() {
        try {
            connectors[0].connect();
        } catch (SessionException re) {
            log.error("Exception from connector connect: " + re.getLocalizedMessage());
        }
        CompletableFuture.runAsync(() -> {
            while (true) {
                if (!icapMessageRepo.isEmpty()) {
                    subscriber.onNext(icapMessageRepo.poll());
                } else {
                    icapMessageRepo.await();
                }
            }
        }, Executors.newSingleThreadExecutor());
    }

    /**
     *
     */
    @Override
    public void checkSession() {
        Arrays.stream(connectors).forEach(connector -> connector.maintainSession());
    }

    /**
     *
     */
    @Scheduled(cron = "${app.session.begin}", zone = "America/New_York")
    public void restartTheSessionTask() {
        tradeVaultService.reloadTradeVault();
        serverStatus.reset();
        Arrays.stream(connectors).forEach(con -> {
            try {
                if (con.getIcapSessionManager().getIcSession().isConnected()) {
                    con.disconnect();
                }
                con.connect();
            } catch (SessionException e) {
                log.error("Session exception caught: {}", e.getLocalizedMessage());
            }
        });
    }

}
