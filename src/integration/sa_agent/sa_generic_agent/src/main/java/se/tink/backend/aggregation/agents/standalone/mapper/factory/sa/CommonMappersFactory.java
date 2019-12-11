package se.tink.backend.aggregation.agents.standalone.mapper.factory.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.common.GoogleDateMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.common.RequestCommonMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.common.SecurityInfoMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.providers.CommonExternalParametersProvider;

public class CommonMappersFactory {

    private final CommonExternalParametersProvider commonExternalParametersProvider;
    private final boolean maualRequest;
    private final String providerName;

    private CommonMappersFactory(
            CommonExternalParametersProvider commonExternalParametersProvider,
            boolean maualRequest,
            String providerName) {
        this.commonExternalParametersProvider = commonExternalParametersProvider;
        this.maualRequest = maualRequest;
        this.providerName = providerName;
    }

    public static CommonMappersFactory newInstance(
            CommonExternalParametersProvider commonExternalParametersProvider,
            boolean maualRequest,
            String providerName) {
        return new CommonMappersFactory(
                commonExternalParametersProvider, maualRequest, providerName);
    }

    public RequestCommonMapper requestCommonMapper() {
        RequestCommonMapper mapper = new RequestCommonMapper();
        mapper.setManualRequest(maualRequest);
        mapper.setProviderName(providerName);
        mapper.setSecurityInfoMapper(securityInfoMapper());
        mapper.setCommonExternalParametersProvider(commonExternalParametersProvider);
        return mapper;
    }

    private SecurityInfoMapper securityInfoMapper() {
        return new SecurityInfoMapper();
    }

    public GoogleDateMapper googleDateMapper() {
        return new GoogleDateMapper();
    }
}
