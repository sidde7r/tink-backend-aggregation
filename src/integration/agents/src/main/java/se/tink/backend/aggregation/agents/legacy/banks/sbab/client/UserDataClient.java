package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.api.client.util.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.ErrorText;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.OperationNames;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.Query;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.Url;
import se.tink.backend.aggregation.agents.banks.sbab.entities.BlancosEntity;
import se.tink.backend.aggregation.agents.banks.sbab.entities.CollateralsEntity;
import se.tink.backend.aggregation.agents.banks.sbab.entities.LoanEntity;
import se.tink.backend.aggregation.agents.banks.sbab.entities.Payload;
import se.tink.backend.aggregation.agents.banks.sbab.entities.QueryFilter;
import se.tink.backend.aggregation.agents.banks.sbab.entities.QueryVariables;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.LoanResponse;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.QueryRequest;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.models.Loan;

@Slf4j
public class UserDataClient extends SBABClient {

    public UserDataClient(Client client, Credentials credentials, String userAgent) {
        super(client, credentials, userAgent);
    }

    public AccountsResponse getAccounts() {
        ClientResponse response =
                createJsonRequestWithCsrf(Url.GRAPGQL_URL)
                        .post(
                                ClientResponse.class,
                                new QueryRequest(
                                        OperationNames.SAVINGS_ACCOUNTS_QUERY,
                                        new QueryVariables(new QueryFilter("CLOSED"), null, null),
                                        Query.SAVINGS_ACCOUNTS));

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new IllegalStateException(
                    ErrorText.HTTP_ERROR
                            + response.getStatus()
                            + ErrorText.HTTP_MESSAGE
                            + response.getEntity(String.class));
        }

        return getAccountEntities(
                response.getEntity(Payload.class).getData().getUser().getAccountsIds());
    }

    private AccountsResponse getAccountEntities(List<String> savingsAccountsNumber) {
        AccountsResponse accountEntities = new AccountsResponse();
        for (String number : savingsAccountsNumber) {
            ClientResponse response =
                    createJsonRequestWithCsrf(Url.GRAPGQL_URL)
                            .post(
                                    ClientResponse.class,
                                    new QueryRequest(
                                            OperationNames.SAVINGS_ACCOUNTS_DETAILS_QUERY,
                                            new QueryVariables(new QueryFilter(null), number, null),
                                            Query.SAVINGS_ACCOUNTS_DETAILS));

            if (response.getStatus() != HttpStatus.SC_OK) {
                throw new IllegalStateException(
                        ErrorText.HTTP_ERROR
                                + response.getStatus()
                                + ErrorText.HTTP_MESSAGE
                                + response.getEntity(String.class));
            }
            Payload entity = response.getEntity(Payload.class);
            accountEntities.add(entity.getData().getUser().getSavingsAccountsDetail());
        }

        return accountEntities;
    }

    public TransactionsResponse fetchCompletedTransactions(String accountNumber)
            throws IllegalStateException {

        final ClientResponse response =
                createJsonRequestWithCsrf(Url.GRAPGQL_URL)
                        .post(
                                ClientResponse.class,
                                new QueryRequest(
                                        OperationNames.SAVINGS_ACCOUNTS_DETAILS_QUERY,
                                        new QueryVariables(
                                                new QueryFilter(null), accountNumber, null),
                                        Query.SAVINGS_ACCOUNTS_DETAILS));

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new IllegalStateException(
                    ErrorText.HTTP_ERROR
                            + response.getStatus()
                            + ErrorText.HTTP_MESSAGE
                            + response.getEntity(String.class));
        }

        Payload entity = response.getEntity(Payload.class);
        TransactionsResponse transactionEntities = new TransactionsResponse();
        transactionEntities.addAll(
                entity.getData()
                        .getUser()
                        .getSavingsAccountsDetail()
                        .getTransfers()
                        .getCompleted());

        return transactionEntities;
    }

    public Map<Account, Loan> getLoans() {
        Optional<LoanResponse> loanResponse = getLoanResponse();

        if (!loanResponse.isPresent()) {
            return Collections.emptyMap();
        }

        Map<Account, Loan> accountLoanMap = Maps.newHashMap();

        for (LoanEntity loan : loanResponse.get()) {
            Optional<Account> tinkAccount = loan.toTinkAccount();
            Optional<Loan> tinkLoan = loan.toTinkLoan();

            if (tinkLoan.isPresent() && tinkAccount.isPresent()) {
                accountLoanMap.put(tinkAccount.get(), tinkLoan.get());
            } else {
                log.error("Could not convert mortgage entity to Tink account/loan");
            }
        }

        return accountLoanMap;
    }

    public void getAuthStatus() {
        ClientResponse response =
                createJsonRequestWithCsrf(Url.GRAPGQL_URL)
                        .post(
                                ClientResponse.class,
                                new QueryRequest(
                                        OperationNames.AUTH_STATUS_POLL_QUERY,
                                        new QueryVariables(new QueryFilter(null), null, null),
                                        Query.AUTH_STATUS));

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private Optional<LoanResponse> getLoanResponse() {

        LoanResponse loanResponse = new LoanResponse();

        getMortgageLoan(loanResponse);
        getBlancoLoan(loanResponse);
        return Optional.ofNullable(loanResponse);
    }

    private void getBlancoLoan(LoanResponse loanResponse) {
        final ClientResponse response =
                createJsonRequestWithCsrf(Url.GRAPGQL_URL)
                        .post(
                                ClientResponse.class,
                                new QueryRequest(
                                        OperationNames.BLANCO_LOAN_ACCOUNTS_QUERY,
                                        new QueryVariables(new QueryFilter(null), null, null),
                                        Query.BLANCO_LOAN_ACCOUNTS));

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new IllegalStateException(
                    ErrorText.HTTP_ERROR
                            + response.getStatus()
                            + ErrorText.HTTP_MESSAGE
                            + response.getEntity(String.class));
        }
        List<BlancosEntity> loans =
                response.getEntity(Payload.class)
                        .getData()
                        .getUser()
                        .getLoans()
                        .getBlanco()
                        .getList();
        for (BlancosEntity blancos : loans) {
            getBlancoLoanDetails(loanResponse, blancos);
        }
    }

    private void getBlancoLoanDetails(LoanResponse loanResponse, BlancosEntity blanco) {
        final ClientResponse response =
                createJsonRequestWithCsrf(Url.GRAPGQL_URL)
                        .post(
                                ClientResponse.class,
                                new QueryRequest(
                                        OperationNames.LOAN_DETAILS_QUERY,
                                        new QueryVariables(
                                                new QueryFilter(null),
                                                null,
                                                blanco.getLoanNumber().toString()),
                                        Query.LOAN_DETAILS));

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new IllegalStateException(
                    ErrorText.HTTP_ERROR
                            + response.getStatus()
                            + ErrorText.HTTP_MESSAGE
                            + response.getEntity(String.class));
        }
        loanResponse.add(response.getEntity(Payload.class).getData().getUser().getLoan());
    }

    private void getMortgageLoan(LoanResponse loanResponse) {
        final ClientResponse response =
                createJsonRequestWithCsrf(Url.GRAPGQL_URL)
                        .post(
                                ClientResponse.class,
                                new QueryRequest(
                                        OperationNames.LOAN_ACCOUNTS_QUERY,
                                        new QueryVariables(new QueryFilter(null), null, null),
                                        Query.LOAN_ACCOUNTS));

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new IllegalStateException(
                    ErrorText.HTTP_ERROR
                            + response.getStatus()
                            + ErrorText.HTTP_MESSAGE
                            + response.getEntity(String.class));
        }
        List<CollateralsEntity> collaterals =
                response.getEntity(Payload.class)
                        .getData()
                        .getUser()
                        .getLoans()
                        .getMortgages()
                        .getCollaterals();

        for (CollateralsEntity col : collaterals) {
            List<LoanEntity> mortgages = col.getMortgages();
            for (LoanEntity loan : mortgages) {
                loanResponse.add(loan);
            }
        }
    }
}
