package se.tink.backend.aggregation.agents.utils.mappers;

import static org.mockito.Mockito.mock;

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

    @Test
    public void fromAggregationMap() {
        mapper.fromAggregationCredentials(mock(se.tink.backend.agents.rpc.Credentials.class));
    }
}
