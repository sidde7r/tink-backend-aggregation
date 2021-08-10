package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.Urls.CONSENT;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.Urls.CONSENTS;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class SpardaAuthApiClient {
    private static final String CONSENT_ID = "consentId";

    private final SpardaRequestBuilder requestBuilder;
    private final String bicCode;

    public ConsentResponse createConsent(
            ConsentRequest consentRequest, URL redirectUrl, URL redirectUrlNotOk) {
        return requestBuilder
                .createRequest(CONSENTS)
                .header(Psd2Headers.Keys.TPP_REDIRECT_URI, redirectUrl)
                .header(Psd2Headers.Keys.TPP_NOK_REDIRECT_URI, redirectUrlNotOk)
                .header("X-BIC", bicCode)
                .post(ConsentResponse.class, consentRequest);
    }

    public ConsentDetailsResponse fetchConsentDetails(String consentId) {
        return requestBuilder
                .createRequestInSession(CONSENT.parameter(CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }
}
