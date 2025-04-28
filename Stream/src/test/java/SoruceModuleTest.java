import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" }
)
@DirtiesContext
public class SoruceModuleTest {
    @Autowired
    private Source source;

    @Test
    public void testSendMessage() {
        source.output().send(MessageBuilder.withPayload("test-message").build());
        // Add assertions to verify message was sent to Kafka topic
    }
}