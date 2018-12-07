package se.tink.backend.aggregation.utils.mapper;

import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;

public class CoreCredentialsMapperTest {
    @Test
    public void fromAggregation() {
        CoreCredentialsMapper.fromAggregationMap.validate();
    }

    @Test
    public void toAggregation() {
        CoreCredentialsMapper.toAggregationMap.validate();
    }
}
