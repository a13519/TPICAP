package net.zousys.mathtrading.interfaces.tpicap;

import com.icap.iConnect.srcMsgs.enums.EICErr;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcSession.ICCallback;
import com.icap.iConnect.srcSession.ICSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Connector;
import net.zousys.mathtrading.interfaces.SessionException;
import net.zousys.mathtrading.interfaces.tpicap.model.ICAPDispatchQueue;
import net.zousys.mathtrading.interfaces.tpicap.model.ICAPMessageRepo;
import net.zousys.mathtrading.interfaces.tpicap.model.ServerSignature;
import net.zousys.mathtrading.interfaces.tpicap.model.ServerStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Slf4j
public class ICAPConnector extends Connector implements ICCallback {
    private ServerSignature serverSignature;
    @Getter
    private ICAPSessionManager icapSessionManager;
    private ICAPMessageRepo icapMessageRepo;
    private ICAPDispatchQueue icapDispatchQueue;
    private List<ICAPMessage> initCommands;
    private List<ICAPMessage> closeCommands;
    private ServerStatus serverStatus;
    public Boolean started = false;

    /**
     * @param serverSignature
     * @param icapMessageRepo
     * @param initCommands
     */
    public ICAPConnector(
            ServerSignature serverSignature,
            ICAPMessageRepo icapMessageRepo,
            ICAPDispatchQueue icapDispatchQueue,
            List<ICAPMessage> initCommands,
            List<ICAPMessage> closeCommands,
            ServerStatus serverStatus) {
        super();
        this.serverSignature = serverSignature;
        this.icapMessageRepo = icapMessageRepo;
        this.icapDispatchQueue = icapDispatchQueue;
        this.initCommands = initCommands;
        this.closeCommands = closeCommands;
        this.serverStatus = serverStatus;
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
        }, Executors.newSingleThreadExecutor());
    }

    /**
     *
     */
    @Override
    public void connect() throws SessionException {
        icapSessionManager.openSession(serverSignature, this);
        icapDispatchQueue.push(initCommands);
        started = true;
        maintainSession();
    }

    /**
     *
     */
    @Override
    public void disconnect() {
        icapSessionManager.closeSession(closeCommands);
    }

    /**
     *
     */
    @Override
    public void maintainSession() {
        if (started) {
            log.debug("Checking session...");
            if (!icapSessionManager.getIcSession().isConnected()) {
                serverStatus.getSessionCut().incrementAndGet();
                log.warn("Session is broken, let's reconnect it...");
                try {
                    disconnect();
                    connect();
                } catch (SessionException e) {
                    log.error("Session exception caught: {}", e.getLocalizedMessage());
                }
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
        icapMessageRepo.push(icapMessage);
    }

    @Override
    public void onError(EICErr eicErr, ICSession icSession) {
        log.error("IConnect API on error calling back: {}", eicErr);
    }
}
