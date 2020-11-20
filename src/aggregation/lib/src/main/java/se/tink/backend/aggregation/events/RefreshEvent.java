package se.tink.backend.aggregation.events;

import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto;

@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class RefreshEvent {
    String providerName;
    String correlationId;
    String marketCode;
    String credentialsId;
    String appId;
    String clusterId;
    String userId;
    @Nullable RefreshResultEventProto.RefreshResultEvent.AdditionalInfo additionalInfo;
    se.tink.libraries.credentials.service.RefreshableItem refreshableItem;
}
