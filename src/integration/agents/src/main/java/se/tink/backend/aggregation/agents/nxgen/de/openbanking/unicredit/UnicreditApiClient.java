package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc.UnicreditConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc.UnicreditScaAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc.UnicreditUserData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc.UnicreditUserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.PathParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.util.UnicreditBaseUtils;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UnicreditApiClient extends UnicreditBaseApiClient {

    public UnicreditApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Credentials credentials,
            boolean requestIsManual) {
        super(client, persistentStorage, credentials, requestIsManual);
    }

    public void authenticate() {

        String password = getCredentials().getField(Key.PASSWORD);

        ConsentResponse consentResponse =
                createRequest(new URL(getConfiguration().getBaseUrl() + Endpoints.CONSENTS))
                        .header(HeaderKeys.X_REQUEST_ID, UnicreditBaseUtils.getRequestId())
                        .header(
                                HeaderKeys.PSU_ID_TYPE,
                                getCredentials().getField(Key.ADDITIONAL_INFORMATION))
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                new URL(getConfiguration().getRedirectUrl()))
                        .header(HeaderKeys.TPP_REDIRECT_PREFERED, false)
                        .post(getConsentResponseType(), getConsentRequest());

        UnicreditUserDataResponse unicreditUserDataResponse =
                getUnicreditUserDataResponse(password, consentResponse.getConsentId());

        updateConsentWithOtp(unicreditUserDataResponse.getScaRedirect());

        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
    }

    public void updateConsentWithOtp(String scaRedirect) {

        getConsentUpdateRequest(new URL(getConfiguration().getBaseUrl() + scaRedirect))
                .put(
                        String.class,
                        new UnicreditScaAuthenticationData(
                                getCredentials().getField(Key.OTP_INPUT)));
    }

    public UnicreditUserDataResponse getUnicreditUserDataResponse(
            String password, String consentId) {

        return getConsentUpdateRequest(
                        new URL(getConfiguration().getBaseUrl() + Endpoints.UPDATE_CONSENT)
                                .parameter(PathParameters.CONSENT_ID, consentId))
                .put(UnicreditUserDataResponse.class, new UnicreditUserData(password));
    }

    private RequestBuilder getConsentUpdateRequest(URL url) {

        return createRequest(url)
                .header(HeaderKeys.X_REQUEST_ID, UnicreditBaseUtils.getRequestId())
                .header(
                        HeaderKeys.PSU_ID_TYPE,
                        getCredentials().getField(Key.ADDITIONAL_INFORMATION))
                .header(HeaderKeys.TPP_REDIRECT_PREFERED, true)
                .header(HeaderKeys.TPP_REDIRECT_URI, new URL(getConfiguration().getRedirectUrl()))
                .header(HeaderKeys.PSU_ID, getCredentials().getField(Key.USERNAME));
    }

    @Override
    protected Class<? extends ConsentResponse> getConsentResponseType() {
        return UnicreditConsentResponse.class;
    }

    @Override
    public URL getScaRedirectUrlFromConsentResponse(ConsentResponse consentResponse) {
        return new URL(getConfiguration().getBaseUrl() + consentResponse.getScaRedirect());
    }

    @Override
    protected String getTransactionsDateFrom() {
        return QueryValues.TRANSACTION_FROM_DATE;
    }
}
