package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.system.rpc.Instrument;

public class SecurityHolding extends BaseResponse {

    public Optional<Instrument> toInstrument(HandelsbankenSEApiClient client) {
        return client.securityHolding(this).flatMap(SecurityHoldingContainer::toInstrument);
    }

    public Optional<URL> toSecurityHolding() {
        return searchLink(HandelsbankenConstants.URLS.Links.SECURITY_HOLDING);
    }
}
