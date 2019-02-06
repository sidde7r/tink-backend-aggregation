package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.Payment;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.authenticator.rpc.lightLogin.LightLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.authenticator.rpc.lightLogin.PasswordLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.rpc.CardDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.rpc.CardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.rpc.CreditCardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.rpc.CustodyAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.rpc.CustodyAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.initialContext.InitialContextRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.initialContext.InitialContextResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.payments.ConfirmedPaymentsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.payments.ConfirmedPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.payments.PaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.payments.UnconfirmedPaymentsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.payments.UnconfirmedPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.rpc.NordeaResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.LogResponseFilter;

public class NordeaV20ApiClient {
    // magic switch to toggle response logging, for beta
    private static final boolean LOG_RESPONSE = false;
    protected final TinkHttpClient client;
    protected final Credentials credentials;
    protected final String marketCode;

    private InitialContextResponse initialContext;

    public NordeaV20ApiClient(TinkHttpClient client, Credentials credentials, String marketCode) {
        this.client = client;
        this.credentials = credentials;
        this.marketCode = marketCode;
    }

    public void setToken(String token) {
        client.addPersistentHeader("x-Security-Token", token);
    }

    private InitialContextResponse fetchInitialContext() {
        if (initialContext == null) {
            initialContext = request(new InitialContextRequest(), InitialContextResponse.class);
        }

        return initialContext;
    }

    public List<ProductEntity> getAccountProductsOfTypes(String... productTypes) {
        return fetchInitialContext().getProductsOfTypes(productTypes);
    }

    public boolean canViewTransactions(Account account) {
        return fetchInitialContext().getData().getProducts().stream()
                .filter(ProductEntity::canView)
                .anyMatch(pe -> Objects.equals(account.getBankIdentifier(), pe.getNordeaAccountIdV2()));
    }

    public TransactionsResponse fetchTransactions(String accountId, String continueKey) {
        return request(new TransactionsRequest(accountId, continueKey), TransactionsResponse.class, LOG_RESPONSE);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(String cardNumber, String continueKey) {
        return request(new CreditCardTransactionsRequest(cardNumber, continueKey),
                CreditCardTransactionsResponse.class, LOG_RESPONSE);
    }

    public LoanDetailsResponse fetchLoanDetails(String accountId) {
        return request(new LoanDetailsRequest(accountId), LoanDetailsResponse.class, LOG_RESPONSE);
    }

    public CardDetailsResponse fetchCardDetails(String accountId) {
        return request(new CardDetailsRequest(accountId), CardDetailsResponse.class, LOG_RESPONSE);
    }

    public LightLoginResponse passwordLogin(String username, String password) throws AuthenticationException,
            AuthorizationException {
        return authRequest(new PasswordLoginRequest(username, password, marketCode), LightLoginResponse.class);
    }

    protected <T extends NordeaResponse> T request(HttpRequest request, Class<T> responseModel, boolean logResponse) {
        if (!logResponse) {
            return validate(client.request(responseModel, request));
        }

        LogResponseFilter logResponseFilter = new LogResponseFilter(responseModel);

        try {
            client.addFilter(logResponseFilter);
            return validate(client.request(responseModel, request));
        } finally {
            client.removeFilter(logResponseFilter);
        }
    }

    protected <T extends NordeaResponse> T request(HttpRequest request, Class<T> responseModel) {
        return request(request, responseModel, false);
    }

    protected <T extends NordeaResponse> T authRequest(HttpRequest request, Class<T> responseModel)
            throws AuthenticationException, AuthorizationException {
        T response = client.request(responseModel, request);
        Optional<String> errorCode = response.getErrorCode();

        if (errorCode.isPresent()) {
            if (NordeaV20Constants.AUTHENTICATION_EXCEPTIONS_BY_CODE.containsKey(errorCode.get())) {
                throw NordeaV20Constants.AUTHENTICATION_EXCEPTIONS_BY_CODE.get(errorCode.get());
            } else if (NordeaV20Constants.AUTHORIZATION_EXCEPTIONS_BY_CODE.containsKey(errorCode.get())) {
                throw NordeaV20Constants.AUTHORIZATION_EXCEPTIONS_BY_CODE.get(errorCode.get());
            }

            return validate(response);
        }

        return response;
    }

    private <T extends NordeaResponse> T validate(T response) {
        if (response == null) {
            return response;
        }

        Optional<String> errorCode = response.getErrorCode();

        if (errorCode.isPresent()) {
            throw new IllegalStateException(NordeaV20Constants.GENERAL_ERROR_MESSAGES_BY_CODE
                    .getOrDefault(errorCode.get(), "Nordea ErrorCode: " + errorCode.get()));
        }

        return response;
    }

    public List<PaymentEntity> getPayments(ProductEntity productEntity, Payment.StatusCode status) {
        Preconditions.checkArgument(productEntity != null && status != null);
        PaymentsResponse response;

        if (status == Payment.StatusCode.CONFIRMED) {
            response = client.request(ConfirmedPaymentsResponse.class,
                    new ConfirmedPaymentsRequest(productEntity.getNordeaAccountIdV2()));
        } else if (status == Payment.StatusCode.UNCONFIRMED) {
            response = client.request(UnconfirmedPaymentsResponse.class,
                    new UnconfirmedPaymentsRequest(productEntity.getNordeaAccountIdV2()));
        } else {
            return null;
        }

        List<PaymentEntity> payments = response.getPayments();

        return payments.stream()
                .filter(pe -> pe.getFromAccountId() != null && Objects.equals(productEntity.getInternalId(), pe.getFromAccountId()))
                .collect(Collectors.toList());
    }

    public List<CustodyAccount> fetchCustodyAccounts() {
        CustodyAccountsResponse response = request(new CustodyAccountsRequest(), CustodyAccountsResponse.class, LOG_RESPONSE);
        return response != null ? response.getCustodyAccounts() : Collections.emptyList();
    }
}
