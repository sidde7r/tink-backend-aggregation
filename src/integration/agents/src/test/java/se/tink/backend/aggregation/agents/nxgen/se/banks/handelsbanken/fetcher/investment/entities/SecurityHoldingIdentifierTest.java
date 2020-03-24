package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SecurityHoldingIdentifierTest {
    private SecurityHoldingIdentifier securityHoldingIdentifier;

    @Test
    public void getTinkType() {

        final String json = "{\n" + "   \"currency\": \"SEK\",\n" + "   \"type\": \"ETF\"\n" + "}";

        securityHoldingIdentifier =
                SerializationUtils.deserializeFromString(json, SecurityHoldingIdentifier.class);

        Assert.assertEquals(InstrumentType.FUND, securityHoldingIdentifier.getTinkType());
    }
}
