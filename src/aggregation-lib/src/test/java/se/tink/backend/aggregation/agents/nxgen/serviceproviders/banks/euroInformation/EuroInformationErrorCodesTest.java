package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.utils.EuroInformationErrorCodes;
import static org.junit.Assert.assertEquals;

public class EuroInformationErrorCodesTest {

    @Test
    public void getByCodeNumber_whenCodeIsUnknown() {
        assertEquals(EuroInformationErrorCodes.NO_ENUM_VALUE, EuroInformationErrorCodes.getByCodeNumber("777777"));
    }
}
