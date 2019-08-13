package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Status;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigner;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenSEBankIdSigner implements BankIdSigner<PaymentRequest> {

    private static final Logger logger =
            LoggerFactory.getLogger(HandelsbankenBankIdAuthenticator.class);

    private final PersistentStorage persistentStorage;
    private final HandelsbankenBaseApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;
    private SessionResponse sessionResponse;

    public HandelsbankenSEBankIdSigner(
            PersistentStorage persistentStorage,
            HandelsbankenBaseApiClient apiClient,
            SessionStorage sessionStorage,
            Credentials credentials) {
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public Optional<String> getAutostartToken() {
        if (Strings.isNullOrEmpty(credentials.getField(CredentialKeys.USERNAME))) {
            logger.error("SSN/USERNAME was passed as empty or null!");
        }
        SessionResponse response = pisAuthorization(credentials.getField(CredentialKeys.USERNAME));
        Uninterruptibles.sleepUninterruptibly(response.getSleepTime(), TimeUnit.MILLISECONDS);

        sessionResponse = response;
        return Optional.ofNullable(response.getAutoStartToken());
    }

    @Override
    public BankIdStatus collect(PaymentRequest toCollect) {

        DecoupledResponse decoupledResponse =
                apiClient.getDecoupled(
                        new URL(sessionResponse.getLinks().getTokenEntity().getHref()));

        if (decoupledResponse.hasError()) {
            switch (decoupledResponse.getError()) {
                case (Errors.INTENT_EXPIRED):
                case (Errors.MBID_ERROR):
                case (Errors.MBID_MAX_POLLING):
                    return BankIdStatus.TIMEOUT;
                case (Errors.NOT_SHB_APPROVED):
                case (Errors.NOT_SHB_ACTIVATED):
                    try {
                        throw AuthorizationError.UNAUTHORIZED.exception(
                                HandelsbankenSEConstants.BankIdUserMessage.ACTIVATION_NEEDED);
                    } catch (AuthorizationException e) {
                        e.printStackTrace();
                    }
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
                persistentStorage.put(
                        PersistentStorageKeys.OAUTH_2_TOKEN, decoupledResponse.toOauthToken());
                return BankIdStatus.DONE;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private SessionResponse pisAuthorization(String ssn) {
        SessionResponse response =
                sessionStorage
                        .get(StorageKeys.PAYMENT_ID, String.class)
                        .map(paymentId -> apiClient.initDecoupledAuthorizationPis(ssn, paymentId))
                        .orElseThrow(() -> new IllegalStateException(Errors.MISSING_PAYMENT_ID));
        sessionStorage.remove(StorageKeys.PAYMENT_ID);

        return response;
    }
}
