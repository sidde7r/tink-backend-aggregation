package se.tink.backend.aggregation.agents.standalone.mapper.common;

import java.util.UUID;
import se.tink.backend.aggregation.agents.standalone.mapper.providers.CommonExternalParametersProvider;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.common.RequestCommon;

public class RequestCommonMapper implements Mapper<RequestCommon, Void> {

    private SecurityInfoMapper securityInfoMapper;

    private boolean manualRequest;
    private String providerName;

    private CommonExternalParametersProvider commonExternalParametersProvider;

    public void setSecurityInfoMapper(SecurityInfoMapper securityInfoMapper) {
        this.securityInfoMapper = securityInfoMapper;
    }

    public void setManualRequest(boolean manualRequest) {
        this.manualRequest = manualRequest;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setCommonExternalParametersProvider(
            CommonExternalParametersProvider commonExternalParametersProvider) {
        this.commonExternalParametersProvider = commonExternalParametersProvider;
    }

    @Override
    public RequestCommon map(Void source, MappingContext mappingContext) {
        RequestCommon.Builder builder = RequestCommon.newBuilder();
        builder.setCorrelationId(UUID.randomUUID().toString());
        builder.setSecurityInfo(securityInfoMapper.map(source, mappingContext));
        builder.setManual(manualRequest);
        builder.putAllExternalParameters(
                commonExternalParametersProvider.buildExternalParametersMap());
        builder.setProviderName(providerName);
        return builder.build();
    }
}
