package se.tink.backend.aggregation.agents.standalone.mapper.factory.agg;

import se.tink.backend.aggregation.agents.standalone.mapper.auth.agg.ThirdPartyAppAuthenticationPayloadMapper;

public class AuthenticationResponseMappersFactory {

    private AuthenticationResponseMappersFactory() {}

    public static AuthenticationResponseMappersFactory newInstance() {
        return new AuthenticationResponseMappersFactory();
    }

    public ThirdPartyAppAuthenticationPayloadMapper thirdPartyAppAuthenticationPayloadMapper() {
        return new ThirdPartyAppAuthenticationPayloadMapper();
    }
}
