package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class SebBankIdAuthenticator implements BankIdAuthenticator<String> {
    private static final AggregationLogger LOG =
            new AggregationLogger(SebBankIdAuthenticator.class);
    private final SebApiClient apiClient;

    private String autoStartToken;
    private String csrfToken;
    private String ssn;

    public SebBankIdAuthenticator(SebApiClient apiClient) {
        this.apiClient = apiClient;
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
            apiClient.setupSession(this.ssn);
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
}
