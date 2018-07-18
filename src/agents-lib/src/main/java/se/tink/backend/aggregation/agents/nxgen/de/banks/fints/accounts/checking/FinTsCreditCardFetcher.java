package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts.SEPAAccount;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.statement.MT940Statement;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsAccountTypeConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.rpc.AccountTypes;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class FinTsCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionDatePaginator<CreditCardAccount> {

    private final FinTsApiClient apiClient;

    public FinTsCreditCardFetcher(FinTsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {

        return apiClient.getSepaAccounts().stream().
                filter(sepaAccount -> AccountTypes.CREDIT_CARD.equals(FinTsAccountTypeConverter.getAccountTypeFor(sepaAccount.getAccountType())))
                .map(SEPAAccount::toTinkCreditCardAccount).collect(Collectors.toList());
    }


    @Override
    public Collection<? extends Transaction> getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
            return apiClient.getTransactions(account.getAccountNumber(), fromDate, toDate).stream()
                    .map(MT940Statement::toTinkTransaction)
                    .collect(Collectors.toList());
    }
}
