package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.api.client.util.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.banks.sbab.entities.LoanEntity;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.LoanResponse;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.models.Loan;

public class UserDataClient extends SBABClient {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String SAVING_URL = SECURE_BASE_URL + "/secure-rest/rest/savings/active";
    private static final String LOAN_URL = SECURE_BASE_URL + "/secure-rest/rest/lan/all";
    private static final String TRANSACTION_URL =
            SECURE_BASE_URL + "/api/transfer-facade-service/transfers";
    private static final String UPCOMING_TRANSACTION_URL =
            TRANSACTION_URL + "/upcoming?accountNumber=%s";
    private static final String COMPLETED_TRANSACTION_URL =
            TRANSACTION_URL + "/completed?accountNumber=%s";

    public UserDataClient(Client client, Credentials credentials, String userAgent) {
        super(client, credentials, userAgent);
    }

    public AccountsResponse getAccounts() {

        ClientResponse response = createJsonRequestWithBearer(SAVING_URL).get(ClientResponse.class);

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new IllegalStateException(
                    "Failed: Http error code:"
                            + response.getStatus()
                            + ", message: "
                            + response.getEntity(String.class));
        }
        AccountsResponse entities = response.getEntity(AccountsResponse.class);
        return entities;
    }

    public TransactionsResponse fetchCompletedTransactions(String accountNumber) throws Exception {
        final String completedTransactionUrl =
                String.format(COMPLETED_TRANSACTION_URL, accountNumber);

        final ClientResponse response =
                createJsonRequestWithBearer(completedTransactionUrl).get(ClientResponse.class);

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new IllegalStateException(
                    "Failed: Http error code:"
                            + response.getStatus()
                            + ", message: "
                            + response.getEntity(String.class));
        }

        return response.getEntity(TransactionsResponse.class);
    }

    public TransactionsResponse fetchUpcomingTransactions(String accountNumber) {
        final String upcomingTransactionUrl =
                String.format(UPCOMING_TRANSACTION_URL, accountNumber);

        ClientResponse response =
                createJsonRequestWithBearer(upcomingTransactionUrl).get(ClientResponse.class);

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new IllegalStateException(
                    "Failed: Http error code:"
                            + response.getStatus()
                            + ", message: "
                            + response.getEntity(String.class));
        }

        return response.getEntity(TransactionsResponse.class);
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
                logger.error("Could not convert mortgage entity to Tink account/loan");
            }
        }

        return accountLoanMap;
    }

    private Optional<LoanResponse> getLoanResponse() {
        LoanResponse loanResponse = createJsonRequestWithBearer(LOAN_URL).get(LoanResponse.class);
        return Optional.ofNullable(loanResponse);
    }
}
