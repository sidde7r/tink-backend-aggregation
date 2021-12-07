package se.tink.backend.aggregation.agents.nxgen.se.openbanking.norwegian;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.NorwegianConstants.RegionId;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.NorwegianMarketConfiguration;

public class NorwegianSEConfiguration implements NorwegianMarketConfiguration {

    @Override
    public String getRegionID() {
        return RegionId.REGION_ID_SE;
    }
}
