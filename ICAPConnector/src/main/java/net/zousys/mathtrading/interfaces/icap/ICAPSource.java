package net.zousys.mathtrading.interfaces.icap;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Connector;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Slf4j
@Component
public class ICAPSource implements Source {

    @Value("${app.pool.connector}")
    private int poolConnector;
    @Value("${app.pool.processor}")
    private int poolProcessor;

    private ExecutorService collectorService;
    private ExecutorService processorService;
    private ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue();
    private Flow.Subscriber<Message> subscriber;

    private Connector[] connectors;
    private Lock lock = new ReentrantLock();
    private Condition write = lock.newCondition();
    @Getter
    private AtomicLong collected = new AtomicLong(0l);
    @Getter
    private AtomicLong consumed = new AtomicLong(0l);
    @Autowired
    public ICAPSource(
            Connector[] connectors,
            ExecutorService collectorService,
            ExecutorService processorService,
            Flow.Subscriber<Message> subscriber) {
        this.connectors = connectors;
        this.collectorService = collectorService;
        this.processorService = processorService;
        this.subscriber = subscriber;
        startDeamon();
    }

    @Override
    public void startDeamon() {
        try {
            connectors[0].connect(queue);
        } catch (RuntimeException re) {
            log.error("Exception from connector connect: "+ re.getLocalizedMessage());
        }
        IntStream.range(0, poolProcessor).forEach(i -> CompletableFuture.runAsync(()-> {
            try {
                while(true) {
                    if (!queue.isEmpty()) {
                        subscriber.onNext(queue.poll());
                        consumed.addAndGet(1);
                    } else {
                        write.await();
                    }
                }
            } catch (Exception e) {
                log.error("Exception from taking message: "+ e.getLocalizedMessage());
            }
        }, processorService));

    }

    @Override
    public void checkSession() {
        Arrays.stream(connectors).forEach(connector -> connector.maintainSession());
    }

    @Override
    public void pollingPush(Message message) {
        collected.addAndGet(1);
        queue.add(message);
    }
}
