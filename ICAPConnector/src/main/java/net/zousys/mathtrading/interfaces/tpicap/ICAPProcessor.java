package net.zousys.mathtrading.interfaces.tpicap;

import com.icap.iConnect.srcMsgs.enums.EICIssueType;
import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.ICExtension;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgElectronicTransaction;
import com.icap.iConnect.srcMsgs.iCMsg.ICTradeData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.tpicap.entity.TradeVaultEntity;
import net.zousys.mathtrading.interfaces.tpicap.model.ServerStatus;
import net.zousys.mathtrading.interfaces.tpicap.model.TradeVault;
import net.zousys.mathtrading.interfaces.tpicap.repository.TradeVaultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;

@Slf4j
@Component("subscriber")
public class ICAPProcessor implements Flow.Subscriber<Message> {
    @Getter
    @Setter
    private boolean active = true;
    @Value("${app.tracing.path.success}")
    private String sucessPath;
    @Value("${app.tracing.path.pending}")
    private String pendingPath;
    @Value("${app.tracing.path.failure}")
    private String failurePath;
    @Value("${app.ackOnBooking}")
    private boolean ackOnBooking;
    @Autowired
    private ExecutorService processorService;
    @Autowired
    private TradeVaultRepository tradeVaultRepository;
    @Autowired
    private TradeVault tradeVault;
    @Autowired
    private ZoneId zoneId;
    @Autowired
    private ServerStatus serverStatus;
    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    /**
     * @param message
     */
    @Override
    public void onNext(Message message) {
        if (active) {
            log.info("PROCESS ------ {}", message.getId());
            log.info("\n" + message.toString());
            switch (((ICAPMessage) message).getICType()) {
                case EICMsgType.eMsgElectronicTransaction: {
                    serverStatus.getBizMessages().incrementAndGet();
                    CompletableFuture.runAsync(() -> {
                        bookTrade((ICMsgElectronicTransaction) message.message());
                    }, processorService);
                    break;
                }
                case EICMsgType.eMsgVoiceTransaction: {
                    serverStatus.getBizMessages().incrementAndGet();
                    break;
                }
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }

    /**
     * @param met
     */
    private void bookTrade(ICMsgElectronicTransaction met) {
        ICTradeData td = met.getTradeData();
        if (!tradeVault.contains(td.getTradeId())) {
            td.getQuantity();
            td.getBrokerId();
            td.getComment();
            td.getCommissionFlag();
            td.getCommissionValue();
            td.getCoupon();
            td.getIssueId();
            td.getMarketId();
            td.getMaturityDate();
            td.getTradeDate();
            td.getTradeTime();
            td.getPriceVector();
            td.getTraderId();
            td.getTradeType();
            td.getIssueId().getIssueLengthByType(EICIssueType.eIssueIsin);
            ICExtension ice = met.getTradeExtension();
            // TODO
            addNewTrade(td.getTradeId());
        } else {
            log.info("Duplicate trade already booked: " + td.getTradeId());
        }
    }

    /**
     * @param tradeId
     */
    @Transactional
    public void addNewTrade(String tradeId) {
        tradeVaultRepository.save(
                TradeVaultEntity.builder()
                        .time(ZonedDateTime.now(zoneId))
                        .tradeId(tradeId)
                        .status(0)
                        .build()
        );
        tradeVault.add(tradeId);
        serverStatus.getTradesBooked().incrementAndGet();
    }

    /**
     * @param id
     */
    private void acknoledge(String id) {
        if (ackOnBooking) {
            // TODO
        }
    }
}
