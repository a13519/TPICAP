package net.zousys.mathtrading.interfaces.tpicap.config;

import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.tpicap.model.TradeVault;
import net.zousys.mathtrading.interfaces.tpicap.repository.TradeVaultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PostDestoryer implements ApplicationListener<ContextClosedEvent> {
    @Autowired
    private TradeVaultRepository tradeVaultRepository;
    @Autowired
    private TradeVault tradeVault;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        // TODO
    }
}
