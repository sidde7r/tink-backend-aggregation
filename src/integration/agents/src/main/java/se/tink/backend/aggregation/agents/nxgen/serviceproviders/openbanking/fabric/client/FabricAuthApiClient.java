package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Urls.API_PSD2_URL;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Urls;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.PsuDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.SelectAuthorizationMethodRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class FabricAuthApiClient {

    private final FabricRequestBuilder requestBuilder;
    private final String baseUrl;

    public ConsentResponse createConsent(URL redirectUrl, ConsentRequest consentRequest) {
        return requestBuilder
                .createRequest(new URL(baseUrl + Urls.CONSENT))
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                .header(HeaderKeys.TPP_REDIRECT_PREFERED, HeaderValues.TPP_REDIRECT_PREFERED)
                .post(ConsentResponse.class, consentRequest);
    }

    public ConsentResponse createConsentForEmbeddedFlow(ConsentRequest consentRequest) {
        return requestBuilder
                .createRequest(new URL(baseUrl + Urls.CONSENT))
                .header(HeaderKeys.TPP_REDIRECT_PREFERED, false)
                .post(ConsentResponse.class, consentRequest);
    }

    public AuthorizationResponse createAuthorizationObject(String authorizationPath) {
        return requestBuilder
                .createRequest(buildUrlWithRawPath(authorizationPath))
                .post(AuthorizationResponse.class);
    }

    public AuthorizationResponse updateAuthorizationWithLoginDetails(
            String authorizationPath, String username, String password) {
        try {

            return requestBuilder
                    .createRequest(buildUrlWithRawPath(authorizationPath))
                    .header(HeaderKeys.PSU_ID, username)
                    .put(
                            AuthorizationResponse.class,
                            new AuthorizationRequest(new PsuDataEntity(password)));
        } catch (HttpResponseException exception) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(exception);
        }
    }

    public AuthorizationResponse updateAuthorizationWithMethodId(
            String authorizationPath, String scaMethodId) {
        return requestBuilder
                .createRequest(buildUrlWithRawPath(authorizationPath))
                .put(
                        AuthorizationResponse.class,
                        new SelectAuthorizationMethodRequest(scaMethodId));
    }

    public AuthorizationStatusResponse updateAuthorizationWithOtpCode(
            String authorizationPath, String smsOtp) {
        try {

            return requestBuilder
                    .createRequest(buildUrlWithRawPath(authorizationPath))
                    .put(
                            AuthorizationStatusResponse.class,
                            new FinalizeAuthorizationRequest(smsOtp));
        } catch (HttpResponseException exception) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(exception);
        }
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return requestBuilder
                .createRequest(
                        new URL(baseUrl + Urls.GET_CONSENT_STATUS)
                                .parameter(IdTags.CONSENT_ID, consentId))
                .get(ConsentStatusResponse.class);
    }

    public ConsentDetailsResponse getConsentDetails(String consentId) {
        return requestBuilder
                .createRequest(
                        new URL(baseUrl + Urls.GET_CONSENT_DETAILS)
                                .parameter(IdTags.CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }

    private URL buildUrlWithRawPath(String endpointPath) {
        return new URL(baseUrl + API_PSD2_URL + endpointPath);
    }
}
