package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.BankIdAutostartTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.BankiIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginProvidersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants.MultiFactorAuthentication.AUTOSTART_TOKEN;

public class CrossKeyBankIdAuthenticator
        implements BankIdAuthenticator<BankIdAutostartTokenResponse> {
    public static final Logger LOGGER = LoggerFactory.getLogger(CrossKeyBankIdAuthenticator.class);

    private final CrossKeyApiClient apiClient;
    private final CrossKeyConfiguration agentConfiguration;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;
    private int counter = 0;

    public CrossKeyBankIdAuthenticator(
            CrossKeyApiClient apiClient,
            CrossKeyConfiguration agentConfiguration,
            SessionStorage sessionStorage,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.agentConfiguration = agentConfiguration;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public BankIdAutostartTokenResponse init(String ssn) throws AuthorizationException {
        handleRequestFailure(apiClient.initSession());
        handleRequestFailure(apiClient.getContent());

        LoginProvidersResponse loginProviders = handleRequestFailure(apiClient.getLoginProviders());
        if (!loginProviders.canUseBankId()) {
            LOGGER.error(
                    "Cannot use BankId authentication: "
                            + SerializationUtils.serializeToString(loginProviders));
            throw new RuntimeException(
                    "Cannot use BankId login method: "
                            + SerializationUtils.serializeToString(loginProviders));
        }

        BankIdAutostartTokenResponse response = apiClient.initBankId();
        sessionStorage.put(AUTOSTART_TOKEN, response.getAutoStartToken());
        return response;
    }

    @Override
    public BankIdStatus collect(BankIdAutostartTokenResponse initBankId)
            throws AuthenticationException, AuthorizationException {
        BankiIdResponse bankiIdResponse = apiClient.collectBankId();

        if (bankiIdResponse.getStatus().isSuccess()) {
            return BankIdStatus.DONE;
        }
        handleRequestFailureForBankId(bankiIdResponse, counter);

        Optional<BankIdStatus> bankIdStatus =
                bankiIdResponse
                        .getStatus()
                        .getErrors()
                        .stream()
                        .map(
                                s ->
                                        CrossKeyConstants.MultiFactorAuthentication
                                                .BANKID_ERROR_MAPPING.getOrDefault(
                                                s, BankIdStatus.FAILED_UNKNOWN))
                        .findAny();

        bankIdStatus
                .filter(s -> s == BankIdStatus.FAILED_UNKNOWN)
                .ifPresent(
                        b ->
                                LOGGER.warn(
                                        "Bank ID Failed with unknown error for response: "
                                                + SerializationUtils.serializeToString(
                                                        bankiIdResponse)));

        // Should never use orElseGet as we do use FAILED_UNKNOWN and log it before if occurs
        return bankIdStatus.orElseGet(() -> BankIdStatus.FAILED_UNKNOWN);
    }

    @Override
    public Optional<String> getAutostartToken() {
        return sessionStorage.get(AUTOSTART_TOKEN, String.class);
    }

    private <T extends CrossKeyResponse> void handleRequestFailureForBankId(T message, int counter)
            throws AuthorizationException {

        counter++;

        // If we tried less than 3 times to collect bank ID status we assume it's timing issue and
        // disregard error.
        if (counter <= 3) {
            return;
        }
        // In case we tried more than 3 times and we still get error, we propagate it
        handleRequestFailure(message);
    }

    private <T extends CrossKeyResponse> T handleRequestFailure(T message)
            throws AuthorizationException {
        if (message.isFailure()) {
            if (message.getStatus()
                    .getErrors()
                    .contains(CrossKeyConstants.MultiFactorAuthentication.NOT_AUTHORIZED_ERROR)) {
                LOGGER.error(
                        "Not authorized url: " + SerializationUtils.serializeToString(message));
            }
            throw AuthorizationError.UNAUTHORIZED.exception();
        }
        return message;
    }
}
