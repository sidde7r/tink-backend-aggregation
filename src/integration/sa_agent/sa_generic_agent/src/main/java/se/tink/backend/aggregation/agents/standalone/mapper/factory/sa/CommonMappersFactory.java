package se.tink.backend.aggregation.agents.standalone.mapper.factory.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.common.GoogleDateMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.common.RequestCommonMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.common.SecurityInfoMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.providers.CommonExternalParametersProvider;

public class CommonMappersFactory {

    private final CommonExternalParametersProvider commonExternalParametersProvider;
    private final boolean maualRequest;

    private CommonMappersFactory(
            CommonExternalParametersProvider commonExternalParametersProvider,
            boolean maualRequest) {
        this.commonExternalParametersProvider = commonExternalParametersProvider;
        this.maualRequest = maualRequest;
    }

    public static CommonMappersFactory newInstance(
            CommonExternalParametersProvider commonExternalParametersProvider,
            boolean maualRequest) {
        return new CommonMappersFactory(commonExternalParametersProvider, maualRequest);
    }

    public RequestCommonMapper requestCommonMapper() {
        RequestCommonMapper mapper = new RequestCommonMapper();
        mapper.setManualRequest(maualRequest);
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
