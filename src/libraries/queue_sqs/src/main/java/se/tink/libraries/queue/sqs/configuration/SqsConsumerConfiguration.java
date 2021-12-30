package se.tink.libraries.queue.sqs.configuration;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SqsConsumerConfiguration {
    private int waitTimeSecond = 1;
    private int maxNumberOfMessages = 1;
    private int visibilityTimeoutSeconds = 300; // 5 minutes
}
