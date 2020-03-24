package se.tink.backend.aggregation.agents.nxgen.se.business.seb.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.entities.UserInformation;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.social.security.SocialSecurityNumber;

public class SebAuthenticator implements BankIdAuthenticator<String> {
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
    public String init(String ssn) throws BankServiceException {
        final AuthenticationResponse response = apiClient.initiateBankId();
        this.ssn = ssn;
        csrfToken = response.getCsrfToken();
        autoStartToken = response.getAutoStartToken();
        return response.getCsrfToken();
    }

    @Override
    public String refreshAutostartToken() throws BankServiceException {
        return init(this.ssn);
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        Preconditions.checkNotNull(Strings.emptyToNull(csrfToken), "Missing CSRF token");

        final AuthenticationResponse response = apiClient.collectBankId(csrfToken);
        csrfToken = response.getCsrfToken();

        final BankIdStatus status =
                Authentication.statusMapper.translate(response.getStatus().toLowerCase()).get();
        final BankIdStatus statusDetails =
                Authentication.hintCodeMapper
                        .translate(Strings.nullToEmpty(response.getHintCode()).toLowerCase())
                        .get();

        if (statusDetails == BankIdStatus.NO_CLIENT) {
            return statusDetails;
        }
        if (status == BankIdStatus.DONE) {
            activateSession();
        } else if (status == BankIdStatus.FAILED_UNKNOWN) {
            return statusDetails;
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
