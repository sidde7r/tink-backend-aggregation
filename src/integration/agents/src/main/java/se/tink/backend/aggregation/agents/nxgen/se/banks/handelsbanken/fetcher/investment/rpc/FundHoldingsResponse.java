package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.FundHoldingsUser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public class FundHoldingsResponse extends BaseResponse {
    private FundHoldingsUser userFundHoldings;

    public FundHoldingsUser getUserFundHoldings() {
        return userFundHoldings;
    }
}
