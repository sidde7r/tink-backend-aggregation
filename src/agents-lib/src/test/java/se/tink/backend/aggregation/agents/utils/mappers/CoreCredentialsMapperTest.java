package se.tink.backend.aggregation.agents.utils.mappers;

import org.junit.Test;

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
