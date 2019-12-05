package se.tink.sa.framework.common.mapper;

import org.springframework.stereotype.Component;
import se.tink.sa.framework.mapper.MappingContext;
import se.tink.sa.framework.mapper.ToDomainMapper;
import se.tink.sa.services.common.RequestCommon;
import se.tink.sa.services.common.ResponseCommon;

@Component
public class RequestToResponseCommonMapper
        implements ToDomainMapper<ResponseCommon, RequestCommon> {

    @Override
    public ResponseCommon mapToTransferModel(RequestCommon source, MappingContext context) {
        ResponseCommon.Builder destBuilder = ResponseCommon.newBuilder();
        destBuilder.setCorrelationId(source.getCorrelationId());
        return destBuilder.build();
    }
}
