package se.tink.backend.aggregation.events;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.nasa.boot.rpc.CredentialsType;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainFinishedProto.CredentialsRefreshCommandChainFinished.CredentialsTypes;

public class CredentialsTypeFinishedEventConverter {
    private static final ImmutableMap<CredentialsType, CredentialsTypes> CREDENTIALS_TYPE_FINISHED_EVENT_MAP =
            ImmutableMap.<CredentialsType, CredentialsTypes>builder()
                    .put(CredentialsType.FRAUD, CredentialsTypes.CREDENTIAL_TYPES_FRAUD)
                    .put(CredentialsType.KEYFOB, CredentialsTypes.CREDENTIAL_TYPES_KEYFOB)
                    .put(CredentialsType.MOBILE_BANKID, CredentialsTypes.CREDENTIAL_TYPES_MOBILE_BANKID)
                    .put(CredentialsType.PASSWORD, CredentialsTypes.CREDENTIAL_TYPES_PASSWORD)
                    .put(CredentialsType.THIRD_PARTY_APP, CredentialsTypes.CREDENTIAL_TYPES_THIRD_PARTY_APP)
                    .build();

    public static CredentialsTypes convert(CredentialsType type) {
        return CREDENTIALS_TYPE_FINISHED_EVENT_MAP.getOrDefault(
                type, CredentialsTypes.CREDENTIAL_TYPES_UNKNOWN);
    }
}
