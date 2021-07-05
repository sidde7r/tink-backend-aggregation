package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancacarige;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorageProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class BancaCarigeApiClient extends CbiGlobeApiClient {

    public BancaCarigeApiClient(
            TinkHttpClient client,
            CbiStorageProvider cbiStorageProvider,
            boolean requestManual,
            CbiGlobeProviderConfiguration providerConfiguration,
            String psuIpAddress,
            RandomValueGenerator randomValueGenerator,
            LocalDateTimeSource localDateTimeSource) {
        super(
                client,
                cbiStorageProvider,
                InstrumentType.ACCOUNTS,
                providerConfiguration,
                requestManual ? psuIpAddress : null,
                randomValueGenerator,
                localDateTimeSource);
    }

    @Override
    public String createRedirectUrl(String state, ConsentType consentType, String authResult) {
        // '?' and '&' need to be encoded
        return redirectUrl
                + CbiGlobeUtils.getEncondedRedirectURIQueryParams(
                        state, consentType.getCode(), authResult);
    }
}
