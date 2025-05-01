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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.stream.IntStream;

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
    @Autowired
    private ExecutorService processorService;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

//            IntStream.range(0, poolProcessor).forEach(i -> CompletableFuture.runAsync(() -> {
//        while (true) {
//            if (!icapMessageRepo.isEmpty()) {
//                subscriber.onNext(icapMessageRepo.poll());
//            } else {
//                icapMessageRepo.await();
//            }
//        }
//    }, processorService));
    @Override
    public void onNext(Message message) {
        if (active) {
            log.info("PROCESS ------ {}", message.getId());
            log.info("\n"+message.toString());
            switch (message.message().getMsgType()) {
                case EICMsgType.eMsgElectronicTransaction: {
                    CompletableFuture.runAsync(() -> {
                        bookTrade((ICMsgElectronicTransaction)message.message());
                    }, processorService);
                    break;
                }
                case EICMsgType.eMsgVoiceTransaction: {
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

    private void bookTrade(ICMsgElectronicTransaction met) {
        ICTradeData td = met.getTradeData();
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
    }
    private void acknoledge(String id) {
        // TODO
    }
}
