package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Status;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigner;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenSEBankIdSigner implements BankIdSigner<PaymentRequest> {

    private static final Logger logger =
            LoggerFactory.getLogger(HandelsbankenBankIdAuthenticator.class);

    private final PersistentStorage persistentStorage;
    private final HandelsbankenBaseApiClient apiClient;
    private final Credentials credentials;
    private SessionResponse autoStartToken;

    public HandelsbankenSEBankIdSigner(
            PersistentStorage persistentStorage,
            HandelsbankenBaseApiClient apiClient,
            Credentials credentials) {
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public Optional<String> getAutostartToken() {
        if (Strings.isNullOrEmpty(credentials.getField(CredentialKeys.USERNAME))) {
            logger.error("SSN/USERNAME was passed as empty or null!");
        }
        Uninterruptibles.sleepUninterruptibly(autoStartToken.getSleepTime(), TimeUnit.MILLISECONDS);
        return Optional.ofNullable(autoStartToken.getAutoStartToken());
    }

    @Override
    public BankIdStatus collect(PaymentRequest paymentRequest) {

        DecoupledResponse decoupledResponse =
                apiClient.getDecoupled(
                        new URL(autoStartToken.getLinks().getTokenEntity().getHref()));

        if (decoupledResponse.hasError()) {
            switch (decoupledResponse.getError()) {
                case (Errors.INTENT_EXPIRED):
                case (Errors.MBID_ERROR):
                case (Errors.MBID_MAX_POLLING):
                    return BankIdStatus.TIMEOUT;
                case (Errors.NOT_SHB_APPROVED):
                case (Errors.BANKID_NOT_SHB_ACTIVATED):
                    try {
                        throw BankIdError.AUTHORIZATION_REQUIRED.exception(
                                HandelsbankenSEConstants.BankIdUserMessage.ACTIVATION_NEEDED);
                    } catch (AuthorizationException e) {
                        logger.error("BankId Authorization error.");
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
                persistentStorage.rotateStorageValue(
                        StorageKeys.PIS_TOKEN, decoupledResponse.toOauthToken());
                return BankIdStatus.DONE;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    public void setAutoStartToken(SessionResponse autoStartToken) {
        this.autoStartToken = autoStartToken;
    }
}
