package se.tink.backend.aggregation.agents.standalone.mapper.common;

import se.tink.backend.aggregation.agents.standalone.mapper.MappingContextKeys;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.common.mapper.ProtoObjSetter;
import se.tink.sa.services.security.SecurityInfo;

public class SecurityInfoMapper implements Mapper<SecurityInfo, Void> {

    @Override
    public SecurityInfo map(Void source, MappingContext mappingContext) {
        SecurityInfo.Builder builder = SecurityInfo.newBuilder();
        if (isTrue(mappingContext.get(MappingContextKeys.PROVIDE_STATE_FLAG))) {
            builder.setState(mappingContext.get(MappingContextKeys.STATE));
        }

        String securityToken = mappingContext.get(MappingContextKeys.SECURITY_TOKEN);
        ProtoObjSetter.setValue(SecurityInfo.Builder::setSecurityToken, builder, securityToken);

        String consentId = mappingContext.get(MappingContextKeys.CONSENT_ID);
        ProtoObjSetter.setValue(SecurityInfo.Builder::setConsentId, builder, consentId);

        return builder.build();
    }

    private boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}
