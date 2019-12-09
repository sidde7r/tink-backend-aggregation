package se.tink.backend.aggregation.agents.standalone.mapper.common;

import se.tink.backend.aggregation.agents.standalone.mapper.MappingContextKeys;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.security.SecurityInfo;

public class SecurityInfoMapper implements Mapper<SecurityInfo, Void> {

    @Override
    public SecurityInfo map(Void source, MappingContext mappingContext) {
        SecurityInfo.Builder builder = SecurityInfo.newBuilder();
        if (isTrue(mappingContext.get(MappingContextKeys.PROVIDE_STATE_FLAG))) {
            builder.setState(mappingContext.get(MappingContextKeys.STATE));
        }

        builder.setSecurityToken(mappingContext.get(MappingContextKeys.SECURITY_TOKEN));
        return builder.build();
    }

    private boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value.booleanValue());
    }
}
