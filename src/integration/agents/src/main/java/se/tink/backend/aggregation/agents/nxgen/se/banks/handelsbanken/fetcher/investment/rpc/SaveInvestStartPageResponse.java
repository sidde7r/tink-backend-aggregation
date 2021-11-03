package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants.URLS.Links;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SaveInvestStartPageResponse extends BaseResponse {

    public URL toHoldingsSummary() {
        return findLink(Links.HOLDINGS_SUMMARY);
    }
}
