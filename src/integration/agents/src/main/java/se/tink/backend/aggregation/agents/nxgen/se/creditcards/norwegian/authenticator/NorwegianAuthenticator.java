package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc.CollectBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc.OrderBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc.OrderBankIdResponse.Error;
import se.tink.backend.aggregation.agents.utils.signicat.SignicatParsingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NorwegianAuthenticator implements BankIdAuthenticator<OrderBankIdResponse> {

    private final NorwegianApiClient apiClient;

    public NorwegianAuthenticator(NorwegianApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public OrderBankIdResponse init(String ssn) throws BankIdException, LoginException {
        String returnUrl = apiClient.fetchLoginReturnUrl();

        String initStartPage = apiClient.fetchBankIdInitPage(returnUrl);
        String bankIdUrl = SignicatParsingUtils.parseBankIdServiceUrl(initStartPage);

        OrderBankIdResponse bankIdResponse = apiClient.orderBankId(bankIdUrl, ssn);

        if (bankIdResponse.isError()) {
            final Error error = bankIdResponse.getError();

            if (error.isBankIdAlreadyInProgress()) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            }

            if (error.isInvalidSsn()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw new IllegalStateException(
                    String.format("Bank ID error: [%s] %s", error.getCode(), error.getMessage()));
        }

        return bankIdResponse;
    }

    @Override
    public BankIdStatus collect(OrderBankIdResponse reference)
            throws AuthenticationException, AuthorizationException {
        try {
            final CollectBankIdRequest collectRequest =
                    new CollectBankIdRequest(reference.getOrderRef());
            final CollectBankIdResponse collectResponse =
                    apiClient.collectBankId(reference.getCollectUrl(), collectRequest);

            final BankIdStatus bankIdStatus = collectResponse.getBankIdStatus();

            if (bankIdStatus != BankIdStatus.DONE) return bankIdStatus;

            // Use the SAML created secret key to authenticate
            final String callbackResponse =
                    apiClient.completeBankId(collectResponse.getCompleteUrl());

            String url = apiClient.completeLogin(callbackResponse);
            apiClient.signicatRedirect(url);
            return bankIdStatus;
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw BankIdError.INTERRUPTED.exception();
            }
            throw e;
        }
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
