package net.zousys.mathtrading.interfaces.tpicap;

import com.icap.iConnect.srcMsgs.enums.EICCompressionType;
import com.icap.iConnect.srcMsgs.enums.EICErr;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcSession.ICCallback;
import com.icap.iConnect.srcSession.ICSession;
import com.icap.iConnect.srcSession.ICSessionMngr;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Message;
import net.zousys.mathtrading.interfaces.SessionException;
import net.zousys.mathtrading.interfaces.tpicap.model.ServerSignature;
import net.zousys.mathtrading.interfaces.tpicap.tracing.ICMessageRecorder;

import java.util.List;
import java.util.concurrent.Flow;

@Slf4j
@Builder
public class ICAPSessionManager implements Flow.Subscriber<ICAPMessage> {
    @Getter
    private ICSession icSession;
    private ServerSignature serverSignature;
    private ICCallback icCallback;
    private ICMessageRecorder icMessageRecorder;

    /**
     * @throws SessionException
     */
    protected void openSession(ServerSignature serverSignature, ICCallback icCallback) throws SessionException {
        this.serverSignature = serverSignature;
        this.icCallback = icCallback;
        icSession = ICSessionMngr.getMngr().createSession(
                serverSignature.getKey(),
                serverSignature.getValue(),
                serverSignature.getHost(),
                serverSignature.getPort(),
                icCallback,
                serverSignature.isSsl());
        icSession.setReconnectInterval(0);
        icSession.setCheckHeartbeatTimeout(130 * 1000);
        icSession.setCompression(EICCompressionType.eCompressedData);

        if (serverSignature.getProxyHost() != null && serverSignature.getProxyPort() != -1) {
            icSession.setProxyHostPort(
                    serverSignature.getHost(),
                    serverSignature.getPort());
        }
        EICErr err = icSession.connect();

        if (EICErr.eErrSuccess == err) {
            log.info("Successful login");
            return;
        } else if (EICErr.eErrSessionConnected == err) {
            log.warn("Session already connected");
            return;
        } else if (EICErr.eErrTimeOut == err) {
            log.info("Timeout sending login");
        } else if (EICErr.eErrHostname == err || EICErr.eErrHostPort == err) {
            log.info("host/port not set up properly");
        } else if (EICErr.eErrSocket == err || EICErr.eErrConnect == err) {
            log.info("Error connecting to host");
        } else if (EICErr.eErrLogin == err) {
            log.info("Login request failed");
        } else if (EICErr.eErrLoginArgs == err) {
            log.info("Login parameters not set up properly");
        }
        throw new SessionException("Exception thrown because of connecting and / or auth failure" + err);
    }

    /**
     * @param icm
     */
    protected void dispath(ICAPMessage icm) {
        if (icm != null) {
            EICErr eicErr = icSession.send(icm.getIcMsg());
            if (eicErr == EICErr.eErrSuccess) {
                log.info("Request has been successfully dispatched: {}", icm.getType());
            } else {
                log.error("Request dipatched with negative ack: " + icm);
            }
        }
    }

    /**
     * @param vRequests
     */
    protected void dispath(List<ICAPMessage> vRequests) {
        if (vRequests != null) {
            vRequests.forEach(icm -> dispath(icm));
        }
    }

    /**
     * @param msgList
     * @return
     */
    protected boolean closeSession(List<ICMsg> msgList) {
        if (msgList != null && msgList.size() > 0) {
            msgList.forEach(msg -> icSession.send(msg));
        }
        icSession.disconnect();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.warn("Closing session, sleep interruption. Program continue: " + e.getLocalizedMessage());
        }
        return true;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(ICAPMessage icapMessage) {
        if (icapMessage != null && icSession.isConnected()) {
            dispath(icapMessage);
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
