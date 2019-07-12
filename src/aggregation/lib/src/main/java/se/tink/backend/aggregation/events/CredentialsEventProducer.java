package se.tink.backend.aggregation.events;

import com.google.common.base.Strings;
import com.google.protobuf.Any;
import java.time.Instant;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainStartedProto.CredentialsRefreshCommandChainStarted;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class CredentialsEventProducer extends AgentWorkerCommand {

    private static final Logger logger = LoggerFactory.getLogger(CredentialsEventProducer.class);
    private final EventProducerServiceClient eventProducerServiceClient;
    private final Credentials credentials;
    private final String appId;

    public CredentialsEventProducer(
            @Nullable EventProducerServiceClient eventProducerServiceClient,
            Credentials credentials,
            String appId) {
        this.eventProducerServiceClient = eventProducerServiceClient;
        this.credentials = credentials;
        this.appId = appId;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        CredentialsRefreshCommandChainStarted data =
                CredentialsRefreshCommandChainStarted.newBuilder()
                        .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                        .setAppId(Strings.nullToEmpty(appId))
                        .setUserId(credentials.getUserId())
                        .setCredentialsId(credentials.getId())
                        .setProviderName(credentials.getProviderName())
                        .build();

        sendEvent(Any.pack(data));
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {
        // NOP
    }

    private void sendEvent(Any data) {
        if (eventProducerServiceClient == null) {
            logger.info(
                    "No EventProducerService service is configured. Skipping posting of event.");
            return;
        }
        eventProducerServiceClient.postEventAsync(data);
    }
}
