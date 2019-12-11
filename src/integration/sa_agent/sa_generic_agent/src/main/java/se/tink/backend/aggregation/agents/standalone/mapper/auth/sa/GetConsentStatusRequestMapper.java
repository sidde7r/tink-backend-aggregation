package se.tink.backend.aggregation.agents.standalone.mapper.auth.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.common.RequestCommonMapper;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.model.auth.GetConsentStatusRequest;

public class GetConsentStatusRequestMapper implements Mapper<GetConsentStatusRequest, String> {

    private RequestCommonMapper requestCommonMapper;

    public void setRequestCommonMapper(RequestCommonMapper requestCommonMapper) {
        this.requestCommonMapper = requestCommonMapper;
    }

    @Override
    public GetConsentStatusRequest map(String source, MappingContext mappingContext) {
        GetConsentStatusRequest.Builder builder = GetConsentStatusRequest.newBuilder();
        builder.setRequestCommon(requestCommonMapper.map(null, mappingContext));
        return builder.build();
    }
}
