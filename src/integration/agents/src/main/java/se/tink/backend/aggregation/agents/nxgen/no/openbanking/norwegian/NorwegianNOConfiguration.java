package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.NorwegianConstants.RegionId;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.NorwegianMarketConfiguration;

public class NorwegianNOConfiguration implements NorwegianMarketConfiguration {

    @Override
    public String getRegionID() {
        return RegionId.REGION_ID_NO;
    }
}
