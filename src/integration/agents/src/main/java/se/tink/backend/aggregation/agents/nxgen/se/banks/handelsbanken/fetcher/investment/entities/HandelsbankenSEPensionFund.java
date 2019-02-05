package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenSEPensionFund extends BaseResponse {

    public Optional<URL> toFundHoldingDetail() {
        return searchLink(HandelsbankenConstants.URLS.Links.FUND_HOLDING_DETAILS);
    }
}
