package se.tink.sa.agent.pt.ob.sibs.mapper.common;

import org.springframework.stereotype.Component;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.common.BigDecimal;

@Component
public class BigDecimalMapper implements Mapper<BigDecimal, String> {

    @Override
    public BigDecimal map(String source, MappingContext mappingContext) {
        java.math.BigDecimal val = new java.math.BigDecimal(source);
        BigDecimal.Builder destBuilder = BigDecimal.newBuilder();
        destBuilder.setScale(val.scale());
        destBuilder.setUnscaledValue(val.unscaledValue().longValue());
        return destBuilder.build();
    }
}
