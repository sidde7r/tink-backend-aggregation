package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.HandelsbankenSEFundAccountHoldingDetail;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.agents.models.Instrument;

public class SecurityHolding extends BaseResponse {

    public Optional<Instrument> toInstrument(HandelsbankenSEApiClient client) {

        Optional<URL> fundDetailsLink = searchLink(HandelsbankenConstants.URLS.Links.FUND_HOLDING_DETAILS);
        if (fundDetailsLink.isPresent()) {
            return client.fundHoldingDetail(this).flatMap(HandelsbankenSEFundAccountHoldingDetail::toInstrument);
        }

        return client.securityHolding(this).flatMap(SecurityHoldingContainer::toInstrument);
    }

    public Optional<URL> toSecurityHolding() {
        return searchLink(HandelsbankenConstants.URLS.Links.SECURITY_HOLDING);
    }

    public Optional<URL> toFundHoldingDetail() {
        return searchLink(HandelsbankenConstants.URLS.Links.FUND_HOLDING_DETAILS);
    }
}
