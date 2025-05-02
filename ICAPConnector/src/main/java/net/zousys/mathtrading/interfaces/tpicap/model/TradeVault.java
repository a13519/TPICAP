package net.zousys.mathtrading.interfaces.tpicap.model;

import lombok.Getter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class TradeVault {
    @Getter
    private Set<String> tradeSet = ConcurrentHashMap.newKeySet();

    /**
     *
     * @param icapTradeId
     */
    public void add(String icapTradeId) {
        if (icapTradeId!= null) {
            tradeSet.add(icapTradeId);
        }
    }

    /**
     *
     * @param icapTradeId
     * @return
     */
    public boolean contains(String icapTradeId) {
        if (icapTradeId!= null) {
            return tradeSet.contains(icapTradeId);
        }
        return false;
    }

    /**
     *
     */
    public void clear() {
        tradeSet.clear();
    }
}
