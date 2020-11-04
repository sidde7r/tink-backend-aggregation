package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Scope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.AccessBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.AliasEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.RequestPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.ToEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.TokenPayloadEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.storage.N26Storage;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableKey;

@AllArgsConstructor
@Slf4j
public class N26AuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final String TOKEN_ID = "tokenId";

    private static final int MAX_REF_ID_LENGTH = 18;

    private final N26ApiClient apiClient;
    private final AgentConfiguration<N26Configuration> configuration;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final N26Storage storage;
    private final Credentials credentials;
    private final RandomValueGenerator randomValueGenerator;

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        if (storage.getAccessToken() == null) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        if (!storage.getAccessTokenExpiryDate().isPresent()) {
            final TokenDetailsResponse accessTokenDetails = getAccessTokenDetails();
            storage.storeAccessTokenExpiryDate(retrieveExpiryAtMsValue(accessTokenDetails));
        }

        if (isAccessTokenExpired()) {
            storage.clear();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference)
            throws AuthenticationException, AuthorizationException {
        Optional<Map<String, String>> maybeCallbackData =
                supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        ThirdPartyAppStatus result;
        if (!maybeCallbackData.isPresent()) {
            result = ThirdPartyAppStatus.TIMED_OUT;
        } else {
            Map<String, String> callbackData = new CaseInsensitiveMap<>(maybeCallbackData.get());
            if (callbackData.containsKey(TOKEN_ID)) {
                storage.storeAccessToken(callbackData.get(TOKEN_ID));
                result = ThirdPartyAppStatus.DONE;

                final TokenDetailsResponse accessTokenDetails = getAccessTokenDetails();
                long expiryAtMsValue = retrieveExpiryAtMsValue(accessTokenDetails);
                storage.storeAccessTokenExpiryDate(expiryAtMsValue);
                credentials.setSessionExpiryDate(new Date(expiryAtMsValue));
            } else {
                result = ThirdPartyAppStatus.AUTHENTICATION_ERROR;
            }
        }
        return ThirdPartyAppResponseImpl.create(result);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        return ThirdPartyAppAuthenticationPayload.of(buildAuthorizeUrl());
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private URL buildAuthorizeUrl() {
        final TokenResponse tokenResponse = sendTokenRequest();
        return URL.of(Url.AUTHORIZATION_URL)
                .parameter(TOKEN_ID, tokenResponse.getTokenRequest().getId());
    }

    private TokenResponse sendTokenRequest() {
        return apiClient.tokenRequest(createTokenRequest());
    }

    @SneakyThrows
    private TokenRequest createTokenRequest() {
        final N26Configuration n26Configuration = configuration.getProviderSpecificConfiguration();
        final AliasEntity aliasEntity =
                new AliasEntity(
                        CertificateUtils.getOrganizationIdentifier(configuration.getQwac()),
                        n26Configuration.getRealmId());
        final ToEntity toEntity = new ToEntity(n26Configuration.getMemberId(), aliasEntity);
        final AccessBodyEntity accessBodyEntity = new AccessBodyEntity(Scope.AIS);
        String refId = randomValueGenerator.generateRandomAlphanumeric(MAX_REF_ID_LENGTH);
        final RequestPayload requestPayload =
                new RequestPayload(
                        strongAuthenticationState.getState(),
                        toEntity,
                        accessBodyEntity,
                        configuration.getRedirectUrl(),
                        refId);
        return new TokenRequest(requestPayload);
    }

    private TokenDetailsResponse getAccessTokenDetails() throws SessionException {
        try {
            return apiClient.tokenDetails(storage.getAccessToken());
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }

    private long retrieveExpiryAtMsValue(TokenDetailsResponse response) throws SessionException {
        return Optional.ofNullable(response)
                .map(TokenDetailsResponse::getToken)
                .map(TokenEntity::getPayload)
                .map(TokenPayloadEntity::getExpiresAtMs)
                .map(Long::valueOf)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    private boolean isAccessTokenExpired() {
        return storage.getAccessTokenExpiryDate()
                .map(expiresAtMs -> expiresAtMs < Instant.now().toEpochMilli())
                .orElse(Boolean.TRUE);
    }
}
