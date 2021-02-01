package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid;

import java.util.Optional;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc.TokenExchangeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CollectorBankIdAuthenticator implements BankIdAuthenticator<String> {
    private final CollectorApiClient apiClient;
    private final SessionStorage sessionStorage;

    public CollectorBankIdAuthenticator(
            CollectorApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        final InitBankIdRequest initBankIdRequest = new InitBankIdRequest(ssn);
        final InitBankIdResponse initBankIdResponse = apiClient.initBankid(initBankIdRequest);
        return initBankIdResponse.getSessionid();
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        PollBankIdResponse response = apiClient.pollBankId(reference);
        BankIdStatus bankIdStatus = getBankIdStatus(response);

        if (bankIdStatus == BankIdStatus.DONE) {
            sessionStorage.put(CollectorConstants.Storage.BEARER_TOKEN, response.getBearerToken());
            TokenExchangeResponse tokenExchangeResponse = apiClient.exchangeToken();
            sessionStorage.put(
                    CollectorConstants.Storage.BEARER_TOKEN,
                    tokenExchangeResponse.getBearerToken());
        }

        return bankIdStatus;
    }

    public BankIdStatus getBankIdStatus(PollBankIdResponse response) {
        String status = response.getStatus();
        switch (status.toLowerCase()) {
            case CollectorConstants.BankIdStatus.OUTSTANDING_TRANSACTION:
            case CollectorConstants.BankIdStatus.STARTED:
            case CollectorConstants.BankIdStatus.USER_SIGN:
                return BankIdStatus.WAITING;
            case CollectorConstants.BankIdStatus.COMPLETE:
                return BankIdStatus.DONE;
            case CollectorConstants.BankIdStatus.NO_CLIENT:
                return BankIdStatus.NO_CLIENT;
            case CollectorConstants.BankIdStatus.CANCELLED:
                return BankIdStatus.CANCELLED;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        // The bank will trigger BankID by the given SSN, showing a QR code confuses users
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) throws SessionException {
        return Optional.empty();
    }
}
