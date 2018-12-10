package se.tink.backend.aggregation.agents.utils.mappers;

import org.junit.Test;

public class CoreUserMapperTest {
    @Test
    public void modelMapper() {
        CoreUserMapper.aggregationUserMap.validate();
    }
}
