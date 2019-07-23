package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.LoginCodes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.UserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SEBAuthenticator implements BankIdAuthenticator<String> {
    private static final AggregationLogger LOG = new AggregationLogger(SEBAuthenticator.class);
    private final SEBApiClient apiClient;
    private String autoStartToken;
    private String nextReference;

    public SEBAuthenticator(SEBApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {
        final BankIdResponse bankIdResponse = apiClient.fetchAutostartToken();

        switch (bankIdResponse.getRfa().toUpperCase()) {
            case LoginCodes.COLLECT_BANKID:
                break;
            case LoginCodes.ALREADY_IN_PROGRESS:
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            default:
                throw new IllegalStateException(
                        String.format(ErrorMessages.UNKNOWN_BANKID_STATUS, bankIdResponse));
        }

        autoStartToken = bankIdResponse.getAutostarttoken();
        return bankIdResponse.getNextRequestEntity().getUri();
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        if (nextReference == null) {
            nextReference = reference;
        }

        final BankIdResponse bankIdResponse = apiClient.collectBankId(nextReference);
        nextReference = bankIdResponse.getNextRequestEntity().getUri();

        switch (bankIdResponse.getRfa().toUpperCase()) {
            case LoginCodes.START_BANKID:
            case LoginCodes.USER_SIGN:
                return BankIdStatus.WAITING;
            case LoginCodes.AUTHENTICATED:
                return BankIdStatus.DONE;
            case LoginCodes.ALREADY_IN_PROGRESS:
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            case LoginCodes.NO_CLIENT:
                return BankIdStatus.NO_CLIENT;
            case LoginCodes.USER_CANCELLED:
                return BankIdStatus.CANCELLED;
            case LoginCodes.AUTHORIZATION_REQUIRED:
                throw BankIdError.AUTHORIZATION_REQUIRED.exception(
                        UserMessage.MUST_AUTHORIZE_BANKID.getKey());
            default:
                LOG.warn(
                        String.format(
                                ErrorMessages.UNKNOWN_BANKID_STATUS,
                                SerializationUtils.serializeToString(bankIdResponse)));
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }
}
