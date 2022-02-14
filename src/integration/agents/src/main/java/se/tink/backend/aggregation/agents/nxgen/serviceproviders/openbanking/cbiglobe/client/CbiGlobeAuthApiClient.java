package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.RequestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdatePsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.SelectAuthorizationMethodRequest;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class CbiGlobeAuthApiClient {

    private final CbiGlobeHttpClient client;
    private final CbiGlobeProviderConfiguration providerConfiguration;
    private final CbiUrlProvider urlProvider;

    public CbiConsentResponse createConsent(ConsentRequest consentRequest) {
        String okFullRedirectUrl = client.buildRedirectUri(true);
        String nokFullRedirectUrl = client.buildRedirectUri(false);

        RequestBuilder request =
                client.createRequestInSession(urlProvider.getConsentsUrl())
                        .header(
                                HeaderKeys.ASPSP_PRODUCT_CODE,
                                providerConfiguration.getAspspProductCode())
                        .header(HeaderKeys.TPP_REDIRECT_URI, okFullRedirectUrl)
                        .header(HeaderKeys.TPP_NOK_REDIRECT_URI, nokFullRedirectUrl);

        return client.makeRequest(
                request,
                HttpMethod.POST,
                CbiConsentResponse.class,
                RequestContext.CONSENT_CREATE,
                consentRequest);
    }

    public CbiConsentStatusResponse fetchConsentStatus(String consentId) {
        RequestBuilder requestBuilder =
                client.createRequestInSession(
                        urlProvider.getConsentsStatusUrl().parameter(IdTags.CONSENT_ID, consentId));

        return client.makeRequest(
                requestBuilder,
                HttpMethod.GET,
                CbiConsentStatusResponse.class,
                RequestContext.TOKEN_IS_VALID,
                null);
    }

    public ConsentDetailsResponse fetchConsentDetails(String consentId) {
        RequestBuilder requestBuilder =
                client.createRequestInSession(
                        urlProvider
                                .getConsentsDetailsUrl()
                                .parameter(IdTags.CONSENT_ID, consentId));

        return client.makeRequest(
                requestBuilder,
                HttpMethod.GET,
                ConsentDetailsResponse.class,
                RequestContext.CONSENT_DETAILS,
                null);
    }

    public CbiConsentResponse selectScaMethod(URL url, SelectAuthorizationMethodRequest body) {
        return client.makeRequest(
                client.createRequestInSession(url)
                        .header(HeaderKeys.OPERATION_NAME, HeaderValues.UPDATE_PSU_DATA),
                HttpMethod.PUT,
                CbiConsentResponse.class,
                RequestContext.CONSENT_UPDATE,
                body);
    }

    public <T> T updatePsuCredentials(
            URL url, UpdatePsuCredentialsRequest body, Class<T> responseClass) {
        return client.makeRequest(
                client.createRequestInSession(url)
                        .header(HeaderKeys.OPERATION_NAME, HeaderValues.UPDATE_PSU_DATA),
                HttpMethod.PUT,
                responseClass,
                RequestContext.PSU_CREDENTIALS_UPDATE,
                body);
    }
}
