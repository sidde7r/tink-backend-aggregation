package se.tink.backend.aggregation.agents.standalone.mapper.factory.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.auth.sa.AuthenticationRequestMapper;

public class AuthenticationRequestMapperFactory {

    private final CommonMappersFactory commonMappersFactory;

    private AuthenticationRequestMapperFactory(CommonMappersFactory commonMappersFactory) {
        this.commonMappersFactory = commonMappersFactory;
    }

    public static AuthenticationRequestMapperFactory newInstance(
            CommonMappersFactory commonMappersFactory) {
        return new AuthenticationRequestMapperFactory(commonMappersFactory);
    }

    public AuthenticationRequestMapper authenticationRequestMapper() {
        AuthenticationRequestMapper mapper = new AuthenticationRequestMapper();
        mapper.setRequestCommonMapper(commonMappersFactory.requestCommonMapper());
        return mapper;
    }
}
