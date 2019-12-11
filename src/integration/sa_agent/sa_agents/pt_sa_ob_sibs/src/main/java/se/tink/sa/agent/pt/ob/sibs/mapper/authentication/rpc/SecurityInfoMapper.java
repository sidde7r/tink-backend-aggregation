package se.tink.sa.agent.pt.ob.sibs.mapper.authentication.rpc;

import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.security.SecurityInfo;

public class SecurityInfoMapper implements Mapper<SecurityInfo, String> {

    @Override
    public SecurityInfo map(String source, MappingContext mappingContext) {
        SecurityInfo.Builder builder = SecurityInfo.newBuilder();
        builder.setConsentId(source);
        return builder.build();
    }
}
