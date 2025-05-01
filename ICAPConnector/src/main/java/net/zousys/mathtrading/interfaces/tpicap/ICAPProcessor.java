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
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.Flow;

@Slf4j
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

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(Message message) {
        if (active) {
            log.info("PROCESS ------ {}", message.getId());
            log.info("\n"+message.toString());
            switch (message.message().getMsgType()) {
                case EICMsgType.eMsgElectronicTransaction: {
                    ICMsgElectronicTransaction met = message.message();
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
}
