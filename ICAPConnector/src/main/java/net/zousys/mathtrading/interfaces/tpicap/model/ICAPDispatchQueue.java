package net.zousys.mathtrading.interfaces.tpicap.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.tpicap.ICAPMessage;
import net.zousys.mathtrading.interfaces.tpicap.config.Constants;
import net.zousys.mathtrading.interfaces.tpicap.config.EssentialConfig;
import net.zousys.mathtrading.interfaces.tpicap.tracing.ICMessageRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class ICAPDispatchQueue {
    @Autowired
    private ICMessageRecorder icMessageRecorder;
    @Autowired
    private EssentialConfig.EnumConfig enumConfig;
    @Autowired
    private MsgClassifier classifier;
    @Value("${app.tracing.message.detailed}")
    private boolean detailed;

    private ConcurrentLinkedQueue<ICAPMessage> queue = new ConcurrentLinkedQueue();
    private Lock lock = new ReentrantLock();
    private Condition write = lock.newCondition();
    @Getter
    private AtomicLong total = new AtomicLong(0l);

    /**
     * @param messages
     */
    public void push(List<ICAPMessage> messages) {
        messages.forEach(m -> push(m));
    }

    /**
     * @param message
     */
    public void push(ICAPMessage message) {
        if (message != null) {
            total.addAndGet(1);
            queue.add(message);
            lock.lock();
            try {
                write.signalAll();
            } finally {
                lock.unlock();
            }
            if (isSerialiable(message)) {
                icMessageRecorder.record(message);
            }
        }
    }

    /**
     *
     */
    public void await() {
        lock.lock();
        try {
            write.await();
        } catch (Exception e) {
            log.error("Exception from await dispatch queue: " + e.getLocalizedMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * @return
     */
    public ICAPMessage poll() {
        total.addAndGet(1);
        return queue.poll();
    }

    /**
     * @param icapMessage
     * @return
     */
    private boolean isSerialiable(ICAPMessage icapMessage) {
        if (enumConfig.getSerializeLevel() == Constants.SerializeLevel.ALL) {
            return true;
        } else {
            return classifier.isQualified(icapMessage.getType());
        }
    }
}
