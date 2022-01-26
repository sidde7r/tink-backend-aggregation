package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc.UnicreditConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.PsuDataEntity;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UnicreditApiClient extends UnicreditBaseApiClient {

    private static final boolean TPP_REDIRECT_PREFERRED_VALUE = false;
    private static final String EMPTY_BODY = "{}";

    UnicreditApiClient(
            TinkHttpClient client,
            UnicreditStorage unicreditStorage,
            UnicreditProviderConfiguration providerConfiguration,
            UnicreditBaseHeaderValues headerValues) {
        super(client, unicreditStorage, providerConfiguration, headerValues);
    }

    @Override
    protected Class<? extends ConsentResponse> getConsentResponseType() {
        return UnicreditConsentResponse.class;
    }

    @Override
    public UnicreditConsentResponse createConsent(String state) {
        return createRequest(new URL(providerConfiguration.getBaseUrl() + Endpoints.CONSENTS))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_ID_TYPE, providerConfiguration.getPsuIdType())
                // "TPP-Redirect-URI" header is mandatory even if "TPP-Redirect_URI" is set to false
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(headerValues.getRedirectUrl())
                                .queryParam(HeaderKeys.STATE, state)
                                .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, TPP_REDIRECT_PREFERRED_VALUE)
                .post(UnicreditConsentResponse.class, getConsentRequest());
    }

    public AuthorizationResponse initializeAuthorization(
            String url, String state, String username) {
        return createRequest(new URL(url))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_ID, username)
                .header(HeaderKeys.PSU_ID_TYPE, providerConfiguration.getPsuIdType())
                // "TPP-Redirect-URI" header is mandatory even if "TPP-Redirect_URI" is set to false
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(headerValues.getRedirectUrl())
                                .queryParam(HeaderKeys.STATE, state)
                                .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, TPP_REDIRECT_PREFERRED_VALUE)
                .post(AuthorizationResponse.class, EMPTY_BODY);
    }

    public AuthorizationResponse authorizeWithPassword(
            String url, String username, String password) {
        return createRequest(new URL(url))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_ID, username)
                .put(
                        AuthorizationResponse.class,
                        new AuthorizationRequest(new PsuDataEntity(password)));
    }

    public AuthorizationResponse finalizeAuthorization(String url, String otp) {
        return createRequest(new URL(url))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .put(AuthorizationResponse.class, new FinalizeAuthorizationRequest(otp));
    }
}
