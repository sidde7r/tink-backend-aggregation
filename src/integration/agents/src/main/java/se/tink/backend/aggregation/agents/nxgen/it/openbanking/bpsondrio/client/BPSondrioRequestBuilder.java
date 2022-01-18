package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpsondrio.client;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class BPSondrioRequestBuilder extends CbiGlobeRequestBuilder {

    public BPSondrioRequestBuilder(
            TinkHttpClient client,
            RandomValueGenerator randomValueGenerator,
            LocalDateTimeSource localDateTimeSource,
            CbiGlobeProviderConfiguration providerConfiguration,
            StrongAuthenticationState strongAuthenticationState,
            String redirectUrl,
            String psuIpAddress) {
        super(
                client,
                randomValueGenerator,
                localDateTimeSource,
                providerConfiguration,
                strongAuthenticationState,
                redirectUrl,
                psuIpAddress);
    }

    @Override
    public String buildRedirectUri(boolean isOk) {
        // '?' and '&' need to be encoded
        return redirectUrl
                + CbiUrlUtils.getEncondedRedirectURIQueryParams(
                        strongAuthenticationState.getState(),
                        isOk ? QueryValues.SUCCESS : QueryValues.FAILURE);
    }
}
