package se.tink.backend.aggregation.agents.utils.mapper;

import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.mappers.CoreUserMapper;

public class CoreUserMapperTest {
    @Test
    public void modelMapper() {
        CoreUserMapper.aggregationUserMap.validate();
    }
}
