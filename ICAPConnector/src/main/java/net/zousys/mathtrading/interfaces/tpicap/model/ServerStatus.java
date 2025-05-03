package net.zousys.mathtrading.interfaces.tpicap.model;

import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@ToString
@Component
public class ServerStatus {
    private AtomicInteger rawMessages = new AtomicInteger();
    private AtomicInteger bizMessages = new AtomicInteger();
    private AtomicInteger tradesBooked = new AtomicInteger();
    private AtomicInteger poolingFiles = new AtomicInteger();
    private AtomicInteger sessionCut = new AtomicInteger();

    /**
     *
     */
    public void reset() {
        rawMessages.set(0);
        bizMessages.set(0);
        tradesBooked.set(0);
        poolingFiles.set(0);
        sessionCut.set(0);
    }
}
