package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.SecurityHoldingsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchSecurityHoldingResponse {
    private SecurityHoldingsEntity securityHoldings;

    public SecurityHoldingsEntity getSecurityHoldings() {
        return securityHoldings;
    }
}
