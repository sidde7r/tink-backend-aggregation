package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankErrorCodes;
import static org.junit.Assert.assertEquals;

public class TargoBankErrorCodesTest {

    @Test
    public void getByCodeNumber_whenCodeIsUnknown() {
        assertEquals(TargoBankErrorCodes.NO_ENUM_VALUE, TargoBankErrorCodes.getByCodeNumber("777777"));
    }
}
