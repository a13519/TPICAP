package net.zousys.mathtrading.interfaces.tpicap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.tpicap.config.Constants;
import net.zousys.mathtrading.interfaces.tpicap.config.EssentialConfig;
import net.zousys.mathtrading.interfaces.tpicap.model.MsgClassifier;
import net.zousys.mathtrading.interfaces.tpicap.tracing.ICMessageRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class ICAPMessageRepo {
    @Autowired
    private ICMessageRecorder icMessageRecorder;
    @Autowired
    private MsgClassifier classifier;
    @Autowired
    private EssentialConfig.EnumConfig enumConfig;
    private ConcurrentLinkedQueue<ICAPMessage> queue = new ConcurrentLinkedQueue();
    private Lock lock = new ReentrantLock();
    private Condition write = lock.newCondition();
    @Getter
    private AtomicLong collected = new AtomicLong(0l);
    @Getter
    private AtomicLong consumed = new AtomicLong(0l);

    /**
     * @param message
     */
    public void push(ICAPMessage message) {
        if (isSerialiable(message)) {
                collected.addAndGet(1);
                queue.add(message);
                lock.lock();
                try {
                    write.signalAll();
                } finally {
                    lock.unlock();
                }
            icMessageRecorder.record(message);
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
            log.error("Exception from await queue: " + e.getLocalizedMessage());
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
        consumed.addAndGet(1);
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
