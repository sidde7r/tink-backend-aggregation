package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Scope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Status;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenBankIdAuthenticator implements BankIdAuthenticator<SessionResponse> {

    private static final Logger logger =
            LoggerFactory.getLogger(HandelsbankenBankIdAuthenticator.class);

    private final HandelsbankenBaseApiClient apiClient;
    private final SessionStorage sessionStorage;
    private OAuth2Token token;
    private String autoStartToken;

    public HandelsbankenBankIdAuthenticator(
            HandelsbankenBaseApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public SessionResponse init(String ssn)
            throws BankIdException, BankServiceException, LoginException {

        if (Strings.isNullOrEmpty(ssn)) {
            logger.error("SSN was passed as empty or null!");
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        try {
            TokenResponse tokenResponse =
                    apiClient.requestClientCredentialGrantTokenWithScope(Scope.AIS);
            // store in session storage so it will ba masked
            sessionStorage.put(
                    HandelsbankenBaseConstants.StorageKeys.CLIENT_TOKEN,
                    tokenResponse.getAccessToken());
            AuthorizationResponse consent =
                    apiClient.initiateConsent(tokenResponse.getAccessToken());

            SessionResponse response =
                    apiClient.initDecoupledAuthorizationAis(ssn, consent.getConsentId());
            this.autoStartToken = response.getAutoStartToken();
            Uninterruptibles.sleepUninterruptibly(response.getSleepTime(), TimeUnit.MILLISECONDS);
            return response;
        } catch (HttpClientException e) {
            throw new BankIdException(BankIdError.UNKNOWN);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                e.getResponse().getBody(ErrorResponse.class).handleErrors();
            }
            throw e;
        }
    }

    @Override
    public BankIdStatus collect(SessionResponse reference)
            throws AuthenticationException, AuthorizationException {

        DecoupledResponse decoupledResponse =
                apiClient.getDecoupled(new URL(reference.getLinks().getTokenEntity().getHref()));

        if (decoupledResponse.hasError()) {
            switch (decoupledResponse.getError()) {
                case (Errors.INTENT_EXPIRED):
                case (Errors.MBID_ERROR):
                case (Errors.MBID_MAX_POLLING):
                    return BankIdStatus.TIMEOUT;
                case (Errors.NOT_SHB_APPROVED):
                    throw LoginError.NOT_CUSTOMER.exception();
                case (Errors.BANKID_NOT_SHB_ACTIVATED):
                    throw BankIdError.AUTHORIZATION_REQUIRED.exception(
                            HandelsbankenBaseConstants.BankIdUserMessage.ACTIVATION_NEEDED);
                default:
                    logger.warn(
                            String.format(
                                    "BankID polling failed with error: %s",
                                    decoupledResponse.getError()));
                    return BankIdStatus.FAILED_UNKNOWN;
            }
        }

        switch (decoupledResponse.getResult()) {
            case Status.IN_PROGRESS:
                return BankIdStatus.WAITING;
            case Status.USER_CANCEL:
                return BankIdStatus.CANCELLED;
            case Status.COMPLETE:
                this.token = decoupledResponse.toOauthToken();
                return BankIdStatus.DONE;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.ofNullable(this.token);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        try {
            logger.info("Refreshing access token");
            final TokenResponse response = apiClient.getRefreshToken(refreshToken);

            return Optional.of(
                    OAuth2Token.create(
                            HandelsbankenBaseConstants.QueryKeys.BEARER,
                            response.getAccessToken(),
                            refreshToken,
                            response.getExpiresIn()));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                logger.info("Refresh token expired, throwing session expired exception");
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
        return Optional.empty();
    }
}
