package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator;

import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.AuthenticateForm;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.configuration.LansforsakringarConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class LansforsakringarAuthenticator implements OAuth2Authenticator {

    private final LansforsakringarApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final LansforsakringarConfiguration lansforsakringarConfiguration;
    private final PersistentStorage persistentStorage;

    public LansforsakringarAuthenticator(
            LansforsakringarApiClient apiClient,
            SessionStorage sessionStorage,
            LansforsakringarConfiguration lansforsakringarConfiguration,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.lansforsakringarConfiguration = lansforsakringarConfiguration;
        this.persistentStorage = persistentStorage;
    }

    public ConsentResponse getConsent() {

        return apiClient
                .createRequest(new URL(LansforsakringarConstants.Urls.CONSENT))
                .header(
                        LansforsakringarConstants.HeaderKeys.PSU_ID,
                        apiClient.getCredentials().getField(Field.Key.USERNAME))
                .header(
                        LansforsakringarConstants.HeaderKeys.PSU_ID_TYPE,
                        LansforsakringarConstants.HeaderValues.PSU_ID_TYPE)
                .header(
                        LansforsakringarConstants.HeaderKeys.TPP_REDIRECT_URI,
                        apiClient.getConfiguration().getRedirectUri())
                .header(LansforsakringarConstants.HeaderKeys.TPP_EXPLICIT_AUTH_PREFERRED, false)
                .post(ConsentResponse.class, LansforsakringarConstants.BodyValues.EMPTY_BODY);
    }

    public ConsentResponse getConsentAuthorizations(ConsentResponse consentResponse) {

        return apiClient
                .createRequest(new URL(LansforsakringarConstants.Urls.CONSENT_PROVIDED).parameter(LansforsakringarConstants.IdTags.CONSENT_ID, consentResponse.getConsentId()))
                .header(
                        LansforsakringarConstants.HeaderKeys.PSU_ID,
                        apiClient.getCredentials().getField(Field.Key.USERNAME))
                .header(
                        LansforsakringarConstants.HeaderKeys.PSU_ID_TYPE,
                        LansforsakringarConstants.HeaderValues.PSU_ID_TYPE)
                .header(
                        LansforsakringarConstants.HeaderKeys.TPP_REDIRECT_URI,
                        apiClient.getConfiguration().getRedirectUri())
                .header(LansforsakringarConstants.HeaderKeys.TPP_EXPLICIT_AUTH_PREFERRED, false)
                .post(ConsentResponse.class, LansforsakringarConstants.BodyValues.EMPTY_BODY);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        ConsentResponse consentResponse = getConsentAuthorizations(getConsent());

        persistentStorage.put(LansforsakringarConstants.StorageKeys.CONSENT_ID, consentResponse);
        persistentStorage.put(StorageKeys.AUTHORIZATION_ID, consentResponse.getAuthorisationId());


        return apiClient.createRequest(new URL(LansforsakringarConstants.Urls.AUTHORIZATION)).queryParam(LansforsakringarConstants.QueryKeys.CLIENT_ID, lansforsakringarConfiguration.getClientId())
                .queryParam(LansforsakringarConstants.QueryKeys.RESPONSE_TYPE, LansforsakringarConstants.QueryValues.RESPONSE_TYPE)
                .queryParam(LansforsakringarConstants.QueryKeys.AUTHORIZATION_ID, consentResponse.getAuthorisationId())
                .queryParam(LansforsakringarConstants.QueryKeys.REDIRECT_URI, lansforsakringarConfiguration.getRedirectUri())
                .queryParam(LansforsakringarConstants.QueryKeys.STATE, state)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .getUrl();
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        final String clientId = apiClient.getConfiguration().getClientId();
        final String clientSecret = apiClient.getConfiguration().getClientSecret();
        final String code2 = persistentStorage.get(StorageKeys.AUTHORIZATION_ID);

        final AuthenticateForm form =
                AuthenticateForm.builder()
                        .setClientId(clientId)
                        .setGrantType(FormValues.AUTHORIZATION_CODE)
                        .setCode(code2)
                        .setClientSecret(clientSecret)
                        .setRedirectUri(apiClient.getConfiguration().getRedirectUri())
                        .build();

        return apiClient.postToken(form);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {
        return null;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(LansforsakringarConstants.StorageKeys.ACCESS_TOKEN, accessToken);
    }
}
