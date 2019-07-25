package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Status;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HandelsbankenBankidAuthenticator implements BankIdAuthenticator<SessionResponse> {

    private final HandelsbankenBaseApiClient apiClient;
    private final SessionStorage sessionStorage;
    private OAuth2Token token;
    private String autoStartToken;

    public HandelsbankenBankidAuthenticator(
            HandelsbankenBaseApiClient apiClient, SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
    }

    @Override
    public SessionResponse init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {

        try {
            SessionResponse response = apiClient.getSession(ssn);
            this.autoStartToken = response.getAutoStartToken();
            Uninterruptibles.sleepUninterruptibly(response.getSleepTime(), TimeUnit.MILLISECONDS);
            return response;
        } catch (HttpClientException e) {
            throw new BankIdException(BankIdError.UNKNOWN);
        }
    }

    @Override
    public BankIdStatus collect(SessionResponse reference)
            throws AuthenticationException, AuthorizationException {

        DecoupledResponse decoupledResponse =
                apiClient.getDecoupled(new URL(reference.getLinks().getTokenEntity().getHref()));

        if (decoupledResponse.getError() != null) {
            switch (decoupledResponse.getError()) {
                case (Errors.INTENT_EXPIRED):
                case (Errors.MBID_ERROR):
                    return BankIdStatus.TIMEOUT;
                case (Errors.INVALID_REQUEST):
                case (Errors.UNAUTHORIZED_CLIENT):
                case (Errors.NOT_SHB_APPROVED):
                    return BankIdStatus.NO_CLIENT;
                case (Errors.MBID_MAX_POLLING):
                    return BankIdStatus.INTERRUPTED;
                default:
                    return BankIdStatus.FAILED_UNKNOWN;
            }
        }

        switch (decoupledResponse.getResult()) {
            case Status.IN_PROGRESS:
                return BankIdStatus.WAITING;
            case Status.USER_CANCEL:
                return BankIdStatus.CANCELLED;
            case Status.COMPLETE:
                sessionStorage.put(StorageKeys.ACCESS_TOKEN, decoupledResponse.toOauthToken());
                sessionStorage.put(StorageKeys.REFRESH_TOKEN, decoupledResponse.getRefreshToken());
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

        TokenResponse response = apiClient.getRefreshToken();

        return Optional.of(OAuth2Token.create(
                HandelsbankenBaseConstants.QueryKeys.BEARER,
                response.getAccessToken(),
                sessionStorage.get(StorageKeys.REFRESH_TOKEN),
                response.getExpiresIn()));
    }

}
