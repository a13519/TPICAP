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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Slf4j
@Component
@Scope("prototype")
public class ICAPConnector extends Connector implements ICCallback {

    @Value("${app.connection.online}")
    private boolean online;

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
     *
     * @param icapMessageRepo
     * @param icapDispatchQueue
     * @param initCommands
     * @param closeCommands
     * @param serverStatus
     */
    @Autowired
    public ICAPConnector(
            ServerSignature serverSignature,
            ICAPMessageRepo icapMessageRepo,
            ICAPDispatchQueue icapDispatchQueue,
            List<ICAPMessage> initCommands,
            List<ICAPMessage> closeCommands,
            ServerStatus serverStatus) {
        super();
        this.icapMessageRepo = icapMessageRepo;
        this.icapDispatchQueue = icapDispatchQueue;
        this.initCommands = initCommands;
        this.closeCommands = closeCommands;
        this.serverStatus = serverStatus;
        this.serverSignature = serverSignature;
        this.icapSessionManager = ICAPSessionManager.builder()
                .serverSignature(serverSignature)
                .icCallback(this)
                .build();

        this.online = online;
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
        if (online) {
            icapSessionManager.openSession(this);
            icapDispatchQueue.push(initCommands);
            maintainSession();
        }
        started = true;
    }

    /**
     *
     */
    @Override
    public void disconnect() {
        if (online) {
            icapSessionManager.closeSession(closeCommands);
        }
    }

    /**
     *
     */
    @Override
    public void maintainSession() {
        if (online) {
//            if (started&&online) {
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
