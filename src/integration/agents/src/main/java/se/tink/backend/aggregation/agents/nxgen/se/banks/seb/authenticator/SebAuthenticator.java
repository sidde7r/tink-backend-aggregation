package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.LoginCodes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.UserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.UserInformation;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.social.security.SocialSecurityNumber;

public class SebAuthenticator implements BankIdAuthenticator<String> {
    private static final AggregationLogger LOG = new AggregationLogger(SebAuthenticator.class);
    private final SebApiClient apiClient;
    private final SebSessionStorage sessionStorage;
    private String autoStartToken;
    private String nextReference;
    private String ssn = null;

    public SebAuthenticator(SebApiClient apiClient, SebSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {
        final BankIdResponse bankIdResponse = apiClient.fetchAutostartToken();
        this.ssn = ssn;

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
        if (Objects.isNull(nextReference)) {
            nextReference = reference;
        }

        final BankIdResponse bankIdResponse = apiClient.collectBankId(nextReference);
        nextReference = bankIdResponse.getNextRequestEntity().getUri();

        switch (bankIdResponse.getRfa().toUpperCase()) {
            case LoginCodes.START_BANKID:
            case LoginCodes.USER_SIGN:
            case LoginCodes.WAITING_FOR_BANKID:
                return BankIdStatus.WAITING;
            case LoginCodes.AUTHENTICATED:
                activateSession();
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

    private void activateSession() throws AuthenticationException, AuthorizationException {
        try {
            apiClient.initiateSession();
        } catch (HttpResponseException e) {
            SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(this.ssn);
            if (!ssn.isValid()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            // Check if the user is younger than 18 and then throw unauthorized exception.
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    && ssn.getAge(LocalDate.now(ZoneId.of("CET"))) < SebConstants.AGE_LIMIT) {
                throw AuthorizationError.UNAUTHORIZED.exception(
                        UserMessage.DO_NOT_SUPPORT_YOUTH.getKey());
            }

            throw e;
        }

        final UserInformation userInformation = apiClient.activateSession();
        // Check that the SSN from the credentials matches the logged in user
        if (!userInformation.getSSN().equals(this.ssn)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        sessionStorage.putUserInformation(userInformation);
    }
}
