package net.zousys.mathtrading.interfaces.tpicap;

import com.icap.iConnect.srcMsgs.enums.EICCompressionType;
import com.icap.iConnect.srcMsgs.enums.EICErr;
import com.icap.iConnect.srcMsgs.iCMsg.ICMsg;
import com.icap.iConnect.srcSession.ICCallback;
import com.icap.iConnect.srcSession.ICSession;
import com.icap.iConnect.srcSession.ICSessionMngr;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.SessionException;
import net.zousys.mathtrading.interfaces.tpicap.config.Constants;
import net.zousys.mathtrading.interfaces.tpicap.config.EssentialConfig;
import net.zousys.mathtrading.interfaces.tpicap.tracing.Recorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class ICAPSessionManager {
    @Value("${app.connection.proxyHost:null}")
    private String proxyHost;
    @Value("${app.connection.proxyPort:-1}")
    private int proxyPort;
    @Value("${app.tracing.path.raw}")
    private String icmsgTraceRoot;
    @Autowired
    private EssentialConfig.EnumConfig enumConfig;
    @Autowired
    private ExecutorService recorderService;
    @Getter
    private ICSession icSession;
    private ServerSignature serverSignature;
    private ICCallback icCallback;

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

        if (proxyHost!=null&&proxyPort!=-1) {
            icSession.setProxyHostPort(proxyHost, proxyPort);
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
     * @param vRequests
     */
    protected void dispath(List<ICAPMessage> vRequests) {
        if (vRequests != null) {
            vRequests.forEach(icm -> {
                EICErr eicErr = icSession.send(icm.getIcMsg());
                if (enumConfig.getContentLevel()== Constants.ContentLevel.OUTBOUND) {
                    Recorder.recordMessage(icm, new File(icmsgTraceRoot).toPath(), recorderService);
                }
                if (eicErr == EICErr.eErrSuccess) {
                    log.info("Request has been successfully dispatched: " + icm);
                } else {
                    log.error("Request dipatched with negative ack: " + icm);
                }
            });
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
}
