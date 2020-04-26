package se.tink.backend.aggregation.agents.utils.mappers;

import org.junit.Before;
import org.junit.Test;

public class CoreCredentialsMapperTest {

    private CoreCredentialsMapper mapper;

    @Before
    public void setup() {
        mapper = CoreCredentialsMapper.getInstance();
    }

    @Test
    public void fromAggregation() {
        mapper.fromAggregationMap.validate();
    }

    @Test
    public void toAggregation() {
        mapper.toAggregationMap.validate();
    }
}
