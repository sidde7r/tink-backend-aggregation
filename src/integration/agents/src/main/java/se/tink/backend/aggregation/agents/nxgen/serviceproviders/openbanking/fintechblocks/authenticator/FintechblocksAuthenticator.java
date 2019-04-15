package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator;

import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.ApiService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.JWTHeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.JWTHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.JWTPayloadValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.AuthJWTPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.ClaimsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.ClaimsInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.ConsentJWTHeader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.JWTHeader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.JWTPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.OpenbankingIntentIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.RequestDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities.RiskEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc.CreateConsentBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc.CreateConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.configuration.FintechblocksConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.utils.JWTUtils;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FintechblocksAuthenticator implements OAuth2Authenticator {
    protected final FintechblocksApiClient apiClient;
    protected final PersistentStorage persistentStorage;
    protected final FintechblocksConfiguration configuration;
    protected final PrivateKey privateKey;

    public FintechblocksAuthenticator(
            FintechblocksApiClient apiClient,
            PersistentStorage persistentStorage,
            FintechblocksConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.privateKey = JWTUtils.getPrivateKey(configuration.getClientSigningKeyPath());
    }

    protected FintechblocksConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        OAuth2Token token = authorizeTPP();

        createConsent(token);

        ClaimsInfoEntity claimsInfoEntity =
                new ClaimsInfoEntity(
                        new OpenbankingIntentIdEntity(
                                persistentStorage.get(StorageKeys.CONSENT_ID)));
        ClaimsEntity claimsEntity = new ClaimsEntity(claimsInfoEntity, claimsInfoEntity);
        AuthJWTPayload authJWTPayload =
                new AuthJWTPayload(
                        claimsEntity, configuration.getClientId(), configuration.getRedirectUri());

        String request =
                String.format(
                        "%s.%s",
                        toBase64String(
                                SerializationUtils.serializeToString(getJwtHeader()).getBytes()),
                        toBase64String(
                                SerializationUtils.serializeToString(authJWTPayload).getBytes()));
        request += "." + toBase64String(RSA.signSha256(privateKey, request.getBytes()));
        return apiClient.buildAuthorizeUrl(state, request);
    }

    protected void createConsent(OAuth2Token token) {
        CreateConsentBody createConsentBody =
                new CreateConsentBody(
                        new RequestDataEntity(
                                Arrays.asList(JWTPayloadValues.PERMISSIONS.split(",")),
                                JWTPayloadValues.EXPIRATION_DATE_TIME,
                                JWTPayloadValues.TRANSACTION_FROM_DATE_TIME,
                                JWTPayloadValues.TRANSACTION_TO_DATE_TIME),
                        new RiskEntity());

        ConsentJWTHeader consentJWTHeader =
                new ConsentJWTHeader(
                        JWTHeaderValues.ALG,
                        JWTHeaderValues.B_64,
                        Arrays.asList(JWTHeaderKeys.B_64, JWTHeaderKeys.IAT, JWTHeaderKeys.ISS),
                        new Date().getTime(),
                        JWTHeaderValues.ISS,
                        JWTHeaderValues.KID);

        String jwt2 =
                String.format(
                        "%s.%s",
                        toBase64String(
                                SerializationUtils.serializeToString(consentJWTHeader).getBytes()),
                        toBase64String(
                                SerializationUtils.serializeToString(createConsentBody)
                                        .getBytes()));
        byte[] signedBytes = RSA.signSha256(privateKey, jwt2.getBytes());
        jwt2 = String.format("%s..%s", jwt2.split("\\.")[0], toBase64String(signedBytes));

        CreateConsentResponse createConsentResponse =
                apiClient.createConsent(createConsentBody, token, jwt2);

        persistentStorage.put(
                StorageKeys.CONSENT_ID, createConsentResponse.getData().getConsentId());
    }

    protected OAuth2Token authorizeTPP() {
        GetTokenForm getTokenForm =
                GetTokenForm.builder()
                        .setGrantType(FormValues.CLIENT_CREDENTIALS)
                        .setScope(FormValues.ACCOUNTS)
                        .setClientAssertionType(FormValues.CLIENT_ASSERTION_TYPE)
                        .setClientAssertion(getJwt())
                        .build();

        return apiClient.authorize(getTokenForm);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        GetTokenForm getTokenForm =
                GetTokenForm.builder()
                        .setGrantType(FormValues.AUTHORIZATION_CODE)
                        .setCode(code)
                        .setRedirectUri(configuration.getRedirectUri())
                        .setClientAssertionType(FormValues.CLIENT_ASSERTION_TYPE)
                        .setClientAssertion(getJwt())
                        .build();
        return apiClient.authorize(getTokenForm);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        throw new IllegalStateException(ErrorMessages.MISSING_REFRESH_TOKEN);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    protected String getJwt() {
        JWTHeader jwtHeader = getJwtHeader();
        JWTPayload jwtPayload =
                new JWTPayload(
                        configuration.getClientId(),
                        configuration.getBaseUrl() + ApiService.TOKEN,
                        String.valueOf((new Date().getTime() + 500 * 1000) / 1000));

        String jwt =
                String.format(
                        "%s.%s",
                        toBase64String(SerializationUtils.serializeToString(jwtHeader).getBytes()),
                        toBase64String(
                                SerializationUtils.serializeToString(jwtPayload).getBytes()));

        return String.format(
                "%s.%s", jwt, toBase64String(RSA.signSha256(privateKey, jwt.getBytes())));
    }

    protected JWTHeader getJwtHeader() {
        return new JWTHeader(JWTHeaderValues.ALG, JWTHeaderValues.TYPE);
    }

    protected String toBase64String(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes).replace("=", "");
    }
}
