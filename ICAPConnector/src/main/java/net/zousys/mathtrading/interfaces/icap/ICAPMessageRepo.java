package net.zousys.mathtrading.interfaces.icap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.icap.tracing.Recorder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class ICAPMessageRepo {
    @Value("${app.tracing.path.raw}")
    private String raw;

    private ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue();
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
        collected.addAndGet(1);
        queue.add(message);
        try {
            Recorder.recordMessage(message.getIcMsg(), new File(raw).toPath());
        } catch (IOException e) {
            log.error("Record ICSMsg error: "+e.getLocalizedMessage());
        }
        lock.lock();
        try {
            write.signalAll();
        } finally {
            lock.unlock();
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
    public Message poll() {
        consumed.addAndGet(1);
        return queue.poll();
    }
}
