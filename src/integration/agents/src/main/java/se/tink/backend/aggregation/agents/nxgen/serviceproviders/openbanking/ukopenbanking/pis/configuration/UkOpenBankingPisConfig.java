package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.Risk;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.enums.MarketCode;

public interface UkOpenBankingPisConfig {

    URL getWellKnownURL();

    String getBaseUrl();

    boolean useMaxAge();

    boolean compatibleWithFundsConfirming();

    Risk getPaymentContext();

    MarketCode getMarketCode();
}
