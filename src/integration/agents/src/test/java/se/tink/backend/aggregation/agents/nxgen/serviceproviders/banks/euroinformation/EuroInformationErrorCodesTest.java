package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationErrorCodes;

public class EuroInformationErrorCodesTest {

    @Test
    public void getByCodeNumber_whenCodeIsUnknown() {
        assertEquals(
                EuroInformationErrorCodes.NO_ENUM_VALUE,
                EuroInformationErrorCodes.getByCodeNumber("777777"));
    }

    @Test
    public void getByCodeNumberWhenCodeIsKnown() {
        assertEquals(
                EuroInformationErrorCodes.DOWNTIME,
                EuroInformationErrorCodes.getByCodeNumber("3000"));
    }
}
