package net.zousys.mathtrading.interfaces.icap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.zousys.mathtrading.interfaces.Message;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.concurrent.Flow;

@Slf4j
public class ICAPProcessor implements Flow.Subscriber<Message>{
    @Getter
    @Setter
    private boolean active = true;
    @Value("${app.trace.path.success}")
    private String sucessPath;
    @Value("${app.trace.path.pending}")
    private String pendingPath;
    @Value("${app.trace.path.failure}")
    private String failurePath;

    private File success = new File(sucessPath);
    private File pending = new File(pendingPath);
    private File failure = new File(failurePath);

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(Message message) {
        if (active) {

        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
