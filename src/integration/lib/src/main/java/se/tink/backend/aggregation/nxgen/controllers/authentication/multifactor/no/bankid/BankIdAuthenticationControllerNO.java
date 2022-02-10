package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.i18n_aggregation.Catalog;

public class BankIdAuthenticationControllerNO implements TypedAuthenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int MAX_ATTEMPTS = 90;
    private static final int DEFAULT_AUTHENTICATION_POLL_INTERVAL_SECONDS = 2000;

    private final BankIdAuthenticatorNO authenticator;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;
    private final int authenticationPollIntervalSeconds;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BankIdAuthenticationControllerNO(
            SupplementalInformationController supplementalInformationController,
            BankIdAuthenticatorNO authenticator,
            Catalog catalog) {
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalInformationController =
                Preconditions.checkNotNull(supplementalInformationController);
        this.catalog = catalog;
        this.authenticationPollIntervalSeconds = DEFAULT_AUTHENTICATION_POLL_INTERVAL_SECONDS;
    }

    public BankIdAuthenticationControllerNO(
            SupplementalInformationController supplementalInformationController,
            BankIdAuthenticatorNO authenticator,
            Catalog catalog,
            int authenticationPollIntervalSeconds) {
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalInformationController =
                Preconditions.checkNotNull(supplementalInformationController);
        this.catalog = catalog;
        this.authenticationPollIntervalSeconds = authenticationPollIntervalSeconds;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.MOBILE_BANKID;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));

        String nationalId = credentials.getField(Field.Key.USERNAME);
        String mobilenumber = credentials.getField(Field.Key.MOBILENUMBER);

        if (Strings.isNullOrEmpty(nationalId) || Strings.isNullOrEmpty(mobilenumber)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String dob = nationalId.substring(0, 6);
        String bankIdReference = authenticator.init(nationalId, dob, mobilenumber);
        handleBankIdReferenceAndPollBankIDStatus(bankIdReference);
        authenticator.finishActivation();
    }

    private void handleBankIdReferenceAndPollBankIDStatus(String bankIdReference) {
        // This should be treated as temporary solution.
        // We should not be blocked by supplemental info.
        Future<?> future = startPolling();
        displayBankIdReference(bankIdReference);
        stopPolling(future);
    }

    private Future<?> startPolling() {
        return executor.submit(this::poll);
    }

    private void poll() throws AuthenticationException, AuthorizationException {
        BankIdStatus status = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            status = authenticator.collect();

            switch (status) {
                case DONE:
                    return;
                case WAITING:
                    logger.info("Waiting for BankID");
                    break;
                case CANCELLED:
                    throw BankIdError.CANCELLED.exception();
                case TIMEOUT:
                    throw BankIdError.TIMEOUT.exception();
                default:
                    logger.warn(String.format("Unknown Norwegian BankIdStatus (%s)", status));
                    throw BankIdError.UNKNOWN.exception();
            }

            Uninterruptibles.sleepUninterruptibly(
                    authenticationPollIntervalSeconds, TimeUnit.MILLISECONDS);
        }

        logger.info(
                String.format("Norwegian BankID timed out internally, last status: %s", status));
        throw BankIdError.TIMEOUT.exception();
    }

    private void displayBankIdReference(String bankIdReference) {
        Field field = NorwegianFields.BankIdReferenceInfo.build(catalog, bankIdReference);
        try {
            supplementalInformationController.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }

    private void stopPolling(Future<?> future) throws BankIdException, LoginException {
        try {
            future.get();
            executor.shutdown();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            logger.error("[BankID] Interrupted exception happened", e);
            throw LoginError.DEFAULT_MESSAGE.exception();
        } catch (ExecutionException e) {
            throwKnownExceptionOrDefault(e);
        }
    }

    private void throwKnownExceptionOrDefault(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof BankIdException) {
            throw (BankIdException) cause;
        } else if (cause instanceof LoginException) {
            throw (LoginException) cause;
        } else if (cause instanceof SupplementalInfoException) {
            throw (SupplementalInfoException) cause;
        } else {
            logger.error("[BankID] Other error occurred while polling bankID", e);
            throw LoginError.DEFAULT_MESSAGE.exception();
        }
    }
}
