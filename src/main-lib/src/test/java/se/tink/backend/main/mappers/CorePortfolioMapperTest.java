package se.tink.backend.main.mappers;

import org.junit.Test;

public class CorePortfolioMapperTest {

    @Test
    public void mappingFromCoreToMain_isValid() {
        CorePortfolioMapper.fromCoreToMainMap.validate();
    }
}
