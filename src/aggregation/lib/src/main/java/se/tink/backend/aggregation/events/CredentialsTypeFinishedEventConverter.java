package se.tink.backend.aggregation.events;

import com.google.common.collect.ImmutableMap;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainFinishedProto.CredentialsRefreshCommandChainFinished.CredentialsTypes;

public class CredentialsTypeFinishedEventConverter {
    private static final ImmutableMap<se.tink.backend.agents.rpc.CredentialsTypes, CredentialsTypes>
            CREDENTIALS_TYPE_FINISHED_EVENT_MAP =
                    ImmutableMap
                            .<se.tink.backend.agents.rpc.CredentialsTypes, CredentialsTypes>
                                    builder()
                            .put(
                                    se.tink.backend.agents.rpc.CredentialsTypes.FRAUD,
                                    CredentialsTypes.CREDENTIAL_TYPES_FRAUD)
                            .put(
                                    se.tink.backend.agents.rpc.CredentialsTypes.KEYFOB,
                                    CredentialsTypes.CREDENTIAL_TYPES_KEYFOB)
                            .put(
                                    se.tink.backend.agents.rpc.CredentialsTypes.MOBILE_BANKID,
                                    CredentialsTypes.CREDENTIAL_TYPES_MOBILE_BANKID)
                            .put(
                                    se.tink.backend.agents.rpc.CredentialsTypes.PASSWORD,
                                    CredentialsTypes.CREDENTIAL_TYPES_PASSWORD)
                            .put(
                                    se.tink.backend.agents.rpc.CredentialsTypes.THIRD_PARTY_APP,
                                    CredentialsTypes.CREDENTIAL_TYPES_THIRD_PARTY_APP)
                            .build();

    public static CredentialsTypes convert(se.tink.backend.agents.rpc.CredentialsTypes type) {
        return CREDENTIALS_TYPE_FINISHED_EVENT_MAP.getOrDefault(
                type, CredentialsTypes.CREDENTIAL_TYPES_UNKNOWN);
    }
}
