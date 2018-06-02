package se.tink.backend.main.mappers;

import org.junit.Test;

public class CoreInstrumentMapperTest {

    @Test
    public void mappingFromCoreToMain_isValid() {
        CoreInstrumentMapper.fromCoreToMainMap.validate();
    }
}
