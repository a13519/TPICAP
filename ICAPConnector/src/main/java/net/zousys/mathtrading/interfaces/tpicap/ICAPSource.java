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
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.stream.IntStream;

/**
 *
 */
@Slf4j
@Component
public class ICAPSource implements Source {
    @Value("${app.pool.connector}")
    private int poolConnector;
    @Autowired
    private ServerStatus serverStatus;
    @Autowired
    private ICAPMessageRepo icapMessageRepo;
    @Autowired
    private TradeVaultService tradeVaultService;
    @Autowired
    private ApplicationContext applicationContext;

    private ExecutorService processorService;
    private ExecutorService connectorService;
    private Flow.Subscriber<Message> subscriber;
    private List<ICAPConnector> connectors = new ArrayList<>();
    /**
     * @param processorService
     * @param connectorService
     * @param subscriber
     */
    @Autowired
    public ICAPSource(
            ExecutorService processorService,
            ExecutorService connectorService,
            Flow.Subscriber<Message> subscriber) {
        this.processorService = processorService;
        this.connectorService = connectorService;
        this.subscriber = subscriber;
    }

    /**
     *
     */
    @Override
    public void startDeamon() {
        IntStream.range(0, poolConnector).forEach(i -> {
            ICAPConnector connector = applicationContext.getBean(ICAPConnector.class);
            connectors.add(connector);
            CompletableFuture.runAsync(() -> {
                try {
                    connector.connect();
                } catch (SessionException re) {
                    log.error("Exception from connector connect: " + re.getLocalizedMessage());
                }
            }, connectorService);
        });

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
        connectors.forEach(connector -> connector.maintainSession());
    }

    /**
     *
     */
    @Scheduled(cron = "${app.session.begin}", zone = "America/New_York")
    public void restartTheSessionTask() {
        tradeVaultService.reloadTradeVault();
        serverStatus.reset();
        connectors.forEach(con -> {
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
