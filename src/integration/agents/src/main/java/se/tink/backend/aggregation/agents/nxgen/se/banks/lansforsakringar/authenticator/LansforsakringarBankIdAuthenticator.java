package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc.BankIdLoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class LansforsakringarBankIdAuthenticator implements BankIdAuthenticator<String> {

    private final LansforsakringarApiClient apiClient;
    private final SessionStorage sessionStorage;

    public LansforsakringarBankIdAuthenticator(
            LansforsakringarApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        sessionStorage.put(StorageKeys.SSN, ssn);

        try {
            return apiClient.initBankIdLogin(ssn).getReference();
        } catch (HttpResponseException e) {

            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            }
            throw BankIdError.UNKNOWN.exception();
        }
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        try {
            BankIdLoginResponse response = apiClient.pollBankIdLogin(reference);
            if (response.getSession() == null) {
                return BankIdStatus.NO_CLIENT;
            }
            sessionStorage.put(StorageKeys.TICKET, response.getSession().getTicket());
            sessionStorage.put(
                    StorageKeys.ENTERPRISE_SERVICE_PRIMARY_SESSION,
                    response.getSession().getEnterpriseServicesPrimarySession());
            sessionStorage.put(StorageKeys.CUSTOMER_NAME, response.getSession().getName());
            return BankIdStatus.DONE;
        } catch (HttpResponseException e) {
            String errorCode = e.getResponse().getHeaders().getFirst(HeaderKeys.ERROR_CODE);
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                switch (errorCode) {
                        // Tyvärr har det uppenterpriseServicesPrimarySessionstått ett tekniskt fel.
                        // Försök igen och kontakta oss om
                        // problemet kvarstår.
                    case "00011":
                        // Du har en pågående inloggning via BankID. Signera inloggningen eller välj
                        // att avbryta den.
                    case "00013":
                        // Skriv in säkerhetskoden till ditt BankID och välj Legitimera.
                    case "00014":
                        return BankIdStatus.WAITING;
                    case "99021":
                        // Error-Message: Dina inloggningsuppgifter stämmer inte. Kontrollera dem
                        // och försök igen. Kontakta oss om problemet kvarstår.
                        // This occurs if the user enters the wrong personal number, e.g.
                        // 188705030142
                        throw BankIdError.USER_VALIDATION_ERROR.exception();
                    default:
                        return BankIdStatus.FAILED_UNKNOWN;
                }
            }
        }

        return BankIdStatus.FAILED_UNKNOWN;
    }

    @Override
    public String refreshAutostartToken()
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        return null;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
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
