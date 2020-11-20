package se.tink.backend.aggregation.events;

import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
