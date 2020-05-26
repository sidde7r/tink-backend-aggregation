package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.UserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.UserInformation;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.social.security.SocialSecurityNumber;

public class SebAuthenticator implements BankIdAuthenticator<String> {
    private static final AggregationLogger LOG = new AggregationLogger(SebAuthenticator.class);
    private final SebApiClient apiClient;
    private final SebSessionStorage sessionStorage;
    private String autoStartToken;
    private String csrfToken;
    private String ssn;

    public SebAuthenticator(SebApiClient apiClient, SebSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {
        final AuthenticationResponse response = apiClient.initiateBankId();
        this.ssn = ssn;
        csrfToken = response.getCsrfToken();
        autoStartToken = response.getAutoStartToken();
        return response.getCsrfToken();
    }

    @Override
    public String refreshAutostartToken()
            throws BankServiceException, AuthorizationException, AuthenticationException {
        return init(ssn);
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        Preconditions.checkNotNull(Strings.emptyToNull(csrfToken), "Missing CSRF token");

        final AuthenticationResponse response = apiClient.collectBankId(csrfToken);
        csrfToken = response.getCsrfToken();

        final BankIdStatus status =
                Authentication.statusMapper.translate(response.getStatus().toLowerCase()).get();
        if (status == BankIdStatus.DONE) {
            if (Authentication.hintCodeMapper
                            .translate(Strings.nullToEmpty(response.getHintCode()).toLowerCase())
                            .get()
                    == BankIdStatus.NO_CLIENT) {
                return BankIdStatus.NO_CLIENT;
            }
            activateSession();
        } else if (status == BankIdStatus.FAILED_UNKNOWN) {
            return Authentication.hintCodeMapper
                    .translate(Strings.nullToEmpty(response.getHintCode()).toLowerCase())
                    .get();
        }

        return status;
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
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }

            // Check if the user is younger than 18 and then throw unauthorized exception.
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    && ssn.getAge(LocalDate.now(ZoneId.of("CET"))) < SebConstants.AGE_LIMIT) {
                throw AuthorizationError.UNAUTHORIZED.exception(
                        UserMessage.DO_NOT_SUPPORT_YOUTH.getKey(), e);
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
