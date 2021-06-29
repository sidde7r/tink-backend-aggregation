package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class ConsorsbankAuthApiClient {

    private static final URL CONSENTS_ENDPOINT = new URL("https://xs2a.consorsbank.de/v1/consents");
    private static final URL CONSENT_ENDPOINT =
            new URL("https://xs2a.consorsbank.de/v1/consents/{consentId}");

    private static final String CONSENT_ID = "consentId";

    private final ConsorsbankRequestBuilder requestBuilder;

    public ConsentResponse createConsent(
            ConsentRequest consentRequest, URL redirectUrl, URL redirectUrlNotOk) {
        return requestBuilder
                .createRequest(CONSENTS_ENDPOINT)
                .header(Psd2Headers.Keys.TPP_REDIRECT_URI, redirectUrl)
                .header(Psd2Headers.Keys.TPP_NOK_REDIRECT_URI, redirectUrlNotOk)
                .post(ConsentResponse.class, consentRequest);
    }

    public ConsentDetailsResponse fetchConsentDetails(String consentId) {
        return requestBuilder
                .createRequest(CONSENT_ENDPOINT.parameter(CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }
}
