package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.UpdateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class BancoPostaApiClient extends CbiGlobeApiClient {

    public BancoPostaApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            boolean requestManual,
            TemporaryStorage temporaryStorage) {
        super(client, persistentStorage, requestManual, temporaryStorage, InstrumentType.ACCOUNTS);
    }

    public ConsentResponse updateConsent(String consentId, UpdateConsentRequest body) {
        return createRequestInSession(Urls.CONSENTS.concat("/" + consentId))
                .header(HeaderKeys.OPERATION_NAME, HeaderValues.UPDATE_PSU_DATA)
                .put(ConsentResponse.class, body);
    }

    @Override
    public ConsentScaResponse createConsent(ConsentRequest consentRequest, String redirectUrl) {
        RequestBuilder rb = super.createConsentRequest(redirectUrl);
        return rb.post(ConsentScaResponse.class, consentRequest);
    }
}
