package se.tink.sa.agent.pt.ob.sibs.mapper.authentication.rpc;

import org.springframework.stereotype.Component;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.security.SecurityInfo;

@Component
public class SecurityInfoMapper implements Mapper<SecurityInfo, String> {

    @Override
    public SecurityInfo map(String source, MappingContext mappingContext) {
        SecurityInfo.Builder builder = SecurityInfo.newBuilder();
        builder.setConsentId(source);
        return builder.build();
    }
}
