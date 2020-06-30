package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Scope;
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
public class N26AuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final String TOKEN_ID = "tokenId";

    private final N26ApiClient apiClient;
    private final AgentConfiguration<N26Configuration> configuration;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final N26Storage storage;

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
        Map<String, String> callbackData = getCallbackData();
        processCallbackData(callbackData);
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        return ThirdPartyAppAuthenticationPayload.of(buildAuthorizeUrl());
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private Map<String, String> getCallbackData() throws AuthorizationException {
        return supplementalInformationHelper
                .waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES)
                .orElseThrow(
                        () ->
                                AuthorizationError.UNAUTHORIZED.exception(
                                        "callbackData wasn't received"));
    }

    private void processCallbackData(Map<String, String> callbackData)
            throws AuthorizationException {
        if (!callbackData.containsKey(TOKEN_ID)) {
            throw AuthorizationError.UNAUTHORIZED.exception("callbackData didn't contain tokenId");
        }

        storage.storeAccessToken(callbackData.get(TOKEN_ID));
    }

    private URL buildAuthorizeUrl() {
        final TokenResponse tokenResponse = sendTokenRequest();
        return URL.of(configuration.getProviderSpecificConfiguration().getAuthorizationUrl())
                .parameter(TOKEN_ID, tokenResponse.getTokenRequest().getId());
    }

    private TokenResponse sendTokenRequest() {
        return apiClient.tokenRequest(createTokenRequest());
    }

    private TokenRequest createTokenRequest() {
        final N26Configuration n26Configuration = configuration.getProviderSpecificConfiguration();
        final AliasEntity aliasEntity =
                new AliasEntity(
                        n26Configuration.getAliasType(),
                        n26Configuration.getAliasValue(),
                        n26Configuration.getRealmId());
        final ToEntity toEntity = new ToEntity(n26Configuration.getMemberId(), aliasEntity);
        final AccessBodyEntity accessBodyEntity = new AccessBodyEntity(Scope.AIS);
        final RequestPayload requestPayload =
                new RequestPayload(
                        strongAuthenticationState.getState(),
                        toEntity,
                        accessBodyEntity,
                        configuration.getRedirectUrl());
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
