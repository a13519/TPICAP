package net.zousys.mathtrading.interfaces.tpicap;

import com.icap.iConnect.srcMsgs.enums.EICErr;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgOrderBookRemove;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgTradeBookRemove;
import com.icap.iConnect.srcSession.ICCallback;
import com.icap.iConnect.srcSession.ICSession;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Connector;
import net.zousys.mathtrading.interfaces.SessionException;
import net.zousys.mathtrading.interfaces.tpicap.model.ServerSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Slf4j
public class ICAPConnector extends Connector implements ICCallback {

    private ServerSignature serverSignature;
    private ICAPSessionManager icapSessionManager;
    private ICAPMessageRepo icapMessageRepo;
    private ICAPDispatchQueue icapDispatchQueue;
    private List<ICAPMessage> initMsgs;
    public Boolean started = false;

    /**
     *
     * @param serverSignature
     * @param icapMessageRepo
     * @param msgs
     */
    public ICAPConnector(
            ServerSignature serverSignature,
            ICAPMessageRepo icapMessageRepo,
            ICAPDispatchQueue icapDispatchQueue,
            List<ICAPMessage> msgs) {
        super();
        this.serverSignature = serverSignature;
        this.icapMessageRepo = icapMessageRepo;
        this.icapDispatchQueue = icapDispatchQueue;
        this.initMsgs = msgs;
        this.icapSessionManager = ICAPSessionManager.builder()
                .serverSignature(serverSignature)
                .icCallback(this).build();

        CompletableFuture.runAsync(() -> {
            while (true) {
                if (!icapDispatchQueue.isEmpty()) {
                    icapSessionManager.onNext(icapDispatchQueue.poll());
                } else {
                    icapDispatchQueue.await();
                }
            }
        }, Executors.newFixedThreadPool(1));
    }

    /**
     *
     */
    @Override
    public void connect() {
        try {
            icapSessionManager.openSession(serverSignature, this);
            icapDispatchQueue.push(initMsgs);
            started = true;
        } catch (SessionException e) {
            throw new RuntimeException(e);
        }
        maintainSession();
    }

    /**
     *
     */
    @Override
    public void disconnect() {
        List<ICMsg> msgList = new ArrayList<>();
        msgList.add(new ICMsgOrderBookRemove());
        msgList.add(new ICMsgTradeBookRemove());
        icapSessionManager.closeSession(msgList);
    }

    /**
     *
     */
    @Override
    public void maintainSession() {
        if (started) {
            log.debug("Checking session...");
            if (!icapSessionManager.getIcSession().isConnected()) {
                log.warn("Session is broken, let's reconnect it...");
                disconnect();
                connect();
            }
        }
    }

    @Override
    public void onConnect(ICSession icSession) {
        log.info("IConnect API Version: {}", icSession.getSoftwareVersion());
    }

    @Override
    public void onDisconnect(boolean b, ICSession icSession) {
        log.info("IConnect API disconnected");
    }

    @Override
    public void onData(ICMsg icMsg, ICSession icSession) {
        ICAPMessage icapMessage = new ICAPMessage(icMsg);
        log.info("IConnect API capture a message: {}", icapMessage.getId());
        icapMessageRepo.push(icapMessage);
    }

    @Override
    public void onError(EICErr eicErr, ICSession icSession) {
        log.error("IConnect API on error calling back: {}", eicErr);
    }
}
