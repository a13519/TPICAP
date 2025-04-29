package net.zousys.mathtrading.interfaces.icap;

import com.icap.iConnect.srcMsgs.enums.EICErr;
import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgOrderBookRemove;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgPositiveLogin;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsgTradeBookRemove;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import com.icap.iConnect.srcSession.ICCallback;
import com.icap.iConnect.srcSession.ICSession;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Connector;
import net.zousys.mathtrading.interfaces.SessionException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ICAPConnector extends Connector implements ICCallback {
    private ServerSignature serverSignature;
    private ICAPSessionManager icapSessionManager;
    private ICAPMessageRepo icapMessageRepo;
    private List<ICMsg> initMsgs;
    public Boolean started = false;

    /**
     * @param serverSignature
     */
    public ICAPConnector(ServerSignature serverSignature, ICAPMessageRepo icapMessageRepo, List<ICMsg> msgs) {
        super();
        this.serverSignature = serverSignature;
        this.icapMessageRepo = icapMessageRepo;
        this.initMsgs = msgs;
    }

    /**
     *
     */
    @Override
    public void connect() {
        icapSessionManager = new ICAPSessionManager(serverSignature, this);
        try {
            icapSessionManager.openSession();
            icapSessionManager.dispath(initMsgs);
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
        icapMessageRepo.push(icapMessage);
        log.info("IConnect API capture a message: {}", icapMessage.getId());
    }

    @Override
    public void onError(EICErr eicErr, ICSession icSession) {
        log.error("IConnect API on error calling back: {}", eicErr);
    }
}
