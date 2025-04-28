package net.zousys.mathtrading.interfaces.icap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class ICAPMessageRepo {
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
    public void push(Message message) {
        collected.addAndGet(1);
        queue.add(message);
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
