package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.rpc;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioResponse {
    private PortfolioResponseEntity mobileResponse;

    public PortfolioResponseEntity getMobileResponse() {
        Preconditions.checkNotNull(mobileResponse);
        mobileResponse.validateSession();
        return mobileResponse;
    }
}
