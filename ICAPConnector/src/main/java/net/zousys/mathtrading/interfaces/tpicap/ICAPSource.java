package net.zousys.mathtrading.interfaces.tpicap;


import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Connector;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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
    @Value("${app.pool.processor}")
    private int poolProcessor;
    @Autowired
    private ICAPMessageRepo icapMessageRepo;

    private ExecutorService collectorService;
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
            ExecutorService collectorService,
            ExecutorService processorService,
            Flow.Subscriber<Message> subscriber) {
        this.connectors = connectors;
        this.collectorService = collectorService;
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
        } catch (RuntimeException re) {
            log.error("Exception from connector connect: " + re.getLocalizedMessage());
        }
        IntStream.range(0, poolProcessor).forEach(i -> CompletableFuture.runAsync(() -> {
            while (true) {
                if (!icapMessageRepo.isEmpty()) {
                    subscriber.onNext(icapMessageRepo.poll());
                } else {
                    icapMessageRepo.await();
                }
            }
        }, processorService));

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
        Arrays.stream(connectors).forEach(con -> {
            if (con.getIcapSessionManager().getIcSession().isConnected()) {
                con.disconnect();
            }
            con.connect();
        });
    }

}
