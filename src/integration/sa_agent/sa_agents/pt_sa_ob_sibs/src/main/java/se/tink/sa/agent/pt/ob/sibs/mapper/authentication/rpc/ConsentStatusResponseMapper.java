package se.tink.sa.agent.pt.ob.sibs.mapper.authentication.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusResponse;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.framework.common.mapper.RequestToResponseCommonMapper;
import se.tink.sa.model.auth.GetConsentStatusResponse;
import se.tink.sa.services.common.RequestCommon;
import se.tink.sa.services.common.ResponseCommon;

@Component
public class ConsentStatusResponseMapper
        implements Mapper<GetConsentStatusResponse, ConsentStatusResponse> {

    @Autowired private RequestToResponseCommonMapper requestToResponseCommonMapper;

    @Override
    public GetConsentStatusResponse map(
            ConsentStatusResponse source, MappingContext mappingContext) {
        GetConsentStatusResponse.Builder destBuilder = GetConsentStatusResponse.newBuilder();

        RequestCommon rc = mappingContext.get(SibsMappingContextKeys.REQUEST_COMMON);
        ResponseCommon responseCommon = requestToResponseCommonMapper.map(rc, mappingContext);
        destBuilder.setResponseCommon(responseCommon);

        destBuilder.setTransactionStatus(source.getTransactionStatus());

        return destBuilder.build();
    }
}
