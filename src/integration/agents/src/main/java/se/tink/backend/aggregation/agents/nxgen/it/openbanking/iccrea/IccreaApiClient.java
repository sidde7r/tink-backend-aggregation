package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorageProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class IccreaApiClient extends CbiGlobeApiClient {

    IccreaApiClient(
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
    public ConsentScaResponse createConsent(
            String state, ConsentType consentType, ConsentRequest consentRequest) {
        RequestBuilder rb = createConsentRequest(state, consentType);
        return rb.post(ConsentScaResponse.class, consentRequest);
    }
}
