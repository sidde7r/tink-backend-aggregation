package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class LoginAgentEventProducer {

    private final EventProducerServiceClient eventProducerServiceClient;
    private final boolean sendAgentLoginCompletedEventsEnabled;
    private static final Logger log = LoggerFactory.getLogger(LoginAgentEventProducer.class);

    @Inject
    public LoginAgentEventProducer(
            EventProducerServiceClient eventProducerServiceClient,
            @Named("sendAgentLoginCompletedEvents") boolean sendAgentLoginCompletedEventsEnabled) {
        this.eventProducerServiceClient = eventProducerServiceClient;
        this.sendAgentLoginCompletedEventsEnabled = sendAgentLoginCompletedEventsEnabled;
    }

    public void sendLoginCompletedEvent(
            IntegrationParameters integrationParameters,
            LoginResult result,
            long elapsedTime,
            AgentLoginCompletedEvent.UserInteractionInformation userInteractionInformation) {

        if (!sendAgentLoginCompletedEventsEnabled) {
            return;
        }

        try {
            AgentLoginCompletedEvent event =
                    AgentLoginCompletedEvent.newBuilder()
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                            .setProviderName(integrationParameters.getProviderName())
                            .setCorrelationId(integrationParameters.getCorrelationId())
                            .setResult(result)
                            .setPassedTime(elapsedTime)
                            .setAppId(integrationParameters.getAppId())
                            .setClusterId(integrationParameters.getClusterId())
                            .setUserId(integrationParameters.getUserId())
                            .setUserInteractionInformation(userInteractionInformation)
                            .build();

            eventProducerServiceClient.postEventAsync(Any.pack(event));
        } catch (Exception e) {
            log.error("Could not push agent login event", e);
        }
    }
}
