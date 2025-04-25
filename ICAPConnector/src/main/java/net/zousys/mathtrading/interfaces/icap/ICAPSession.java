package net.zousys.mathtrading.interfaces.icap;

import com.icap.iConnect.srcMsgs.enums.EICCompressionType;
import com.icap.iConnect.srcMsgs.enums.EICErr;
import com.icap.iConnect.srcSession.ICCallback;
import com.icap.iConnect.srcSession.ICSession;
import com.icap.iConnect.srcSession.ICSessionMngr;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.SessionException;

@Slf4j
public class ICAPSession {
    @Getter
    private ICSession icSession;
    private ServerSignature serverSignature;
    private ICCallback icCallback;
    protected ICAPSession(ServerSignature serverSignature, ICCallback icCallback) {
        this.serverSignature = serverSignature;
        this.icCallback = icCallback;
    }

    /**
     *
     * @throws SessionException
     */
    protected void openSession() throws SessionException {
        icSession = ICSessionMngr.getMngr().createSession(
                serverSignature.getKey(),
                serverSignature.getValue(),
                serverSignature.getHost(),
                serverSignature.getPort(),
                icCallback,
                serverSignature.isSsl());
        icSession.setReconnectInterval(0);
        icSession.setCheckHeartbeatTimeout(30 * 1000);
        icSession.setCompression(EICCompressionType.eCompressedData);

        EICErr err = icSession.connect();
        if (EICErr.eErrSuccess == err) {
            log.info("Successful login");
            return ;
        } else if (EICErr.eErrSessionConnected == err) {
            log.warn("Session already connected");
            return ;
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
        throw new SessionException("Exception thrown because of connecting and / or auth failure"+err);
    }
}
