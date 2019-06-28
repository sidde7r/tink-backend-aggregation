package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Status;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenBaseAuthenticator implements BankIdAuthenticator<SessionResponse> {

    private final HandelsbankenBaseApiClient apiClient;
    private final SessionStorage sessionStorage;

    private String autoStartToken;

    public HandelsbankenBaseAuthenticator(
            HandelsbankenBaseApiClient apiClient, SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
    }

    public void authenticate(Credentials credentials) {
        sessionStorage.put(
                StorageKeys.ACCESS_TOKEN, credentials.getField(StorageKeys.ACCESS_TOKEN));
    }

    @Override
    public SessionResponse init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {

        try {
            SessionResponse response = apiClient.buildAuthorizeUrl(ssn);
            this.autoStartToken = response.getAutoStartToken();
            try {
                Thread.sleep(response.getSleepTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return response;
        } catch (HttpClientException e) {
            throw new BankIdException(BankIdError.UNKNOWN);
        }
    }

    @Override
    public BankIdStatus collect(SessionResponse reference)
            throws AuthenticationException, AuthorizationException {

        DecoupledResponse decoupledResponse =
                apiClient.getDecoupled(new URL(reference.getLinks().getToken().getHref()));

        if (decoupledResponse.getError() != null) {
            String error = decoupledResponse.getError();
            if (error.equals(Errors.INTENT_EXPIRED) || error.equals(Errors.MBID_ERROR)) {
                return BankIdStatus.TIMEOUT;
            } else if (error.equals(Errors.INVALID_REQUEST)
                    || error.equals(Errors.NOT_SHB_APPROVED)
                    || error.equals(Errors.UNAUTHORIZED_CLIENT)) {
                return BankIdStatus.NO_CLIENT;
            } else if (error.equals(Errors.MBID_MAX_POLLING)) {
                return BankIdStatus.INTERRUPTED;
            } else {
                return BankIdStatus.FAILED_UNKNOWN;
            }
        }

        if (decoupledResponse.getResult().equals(Status.IN_PROGRESS)) {
            return BankIdStatus.WAITING;
        } else if (decoupledResponse.getResult().equals(Status.USER_CANCEL)) {
            return BankIdStatus.CANCELLED;
        } else if (decoupledResponse.getResult().equals(Status.COMPLETE)) {
            sessionStorage.put(StorageKeys.ACCESS_TOKEN, decoupledResponse.getAccessToken());
            sessionStorage.put(StorageKeys.REFRESH_TOKEN, decoupledResponse.getRefreshToken());
            return BankIdStatus.DONE;
        } else {
            return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }
}
