package net.zousys.mathtrading.interfaces.tpicap.service;

import net.zousys.mathtrading.interfaces.tpicap.model.TradeVault;
import net.zousys.mathtrading.interfaces.tpicap.repository.TradeVaultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TradeVaultService {
    @Autowired
    private TradeVaultRepository tradeVaultRepository;
    @Autowired
    private ZoneId zoneId;
    @Autowired
    private TradeVault tradeVault;

    @Transactional
    public void reloadTradeVault() {
        LocalDate localDate = LocalDate.now(zoneId);
        tradeVaultRepository.deleteAllExceptDate(localDate);
        tradeVault.clear();
        tradeVaultRepository.findAll().stream()
                .forEach(trade -> tradeVault.add(trade.getTradeId()));
    }
}
