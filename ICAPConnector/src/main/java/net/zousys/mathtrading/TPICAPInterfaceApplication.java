package net.zousys.mathtrading;

import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.tpicap.service.PTFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class TPICAPInterfaceApplication implements CommandLineRunner {
    @Autowired
    private PTFService ptfService;

    public static void main(String[] args) {
        SpringApplication.run(TPICAPInterfaceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ptfService.startPTFInterface();
    }

    @EventListener(ApplicationStartedEvent.class)
    void logStartEvent() {
        log.info("The TPICAP Interface Application has started...");
    }
}
