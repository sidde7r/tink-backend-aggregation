package se.tink.backend.aggregation.nxgen.controllers.refresh;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.constants.MarketCode;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.transfer.Transfer;

@VisibleForTesting
public class UpdateControllerTestImpl extends UpdateController {
    private Set<Account> accounts = Sets.newHashSet();

    public UpdateControllerTestImpl(MarketCode market, String currency) {
        super(Mockito.mock(AgentContext.class), market, currency, Mockito.mock(Credentials.class));
    }

    @Override
    @VisibleForTesting
    public <A extends Account> boolean updateAccount(A account) {
        if (!Objects.equals(account.getBalance().getCurrency(), currency)) {
            return false;
        }
        System.out.println("Updating account: " +  toString(account));
        accounts.add(account);
        return true;
    }
//
//    @Override
//    @VisibleForTesting
//    public boolean updateAccount(LoanAccount account) {
//        return updateAccount((Account) account);
//    }
//
//    @Override
//    @VisibleForTesting
//    public boolean updateAccount(InvestmentAccount account) {
//        return updateAccount((Account) account);
//    }

    @Override
    @VisibleForTesting
    public <A extends Account> boolean updateTransactions(A account, Collection<AggregationTransaction> transactions) {
//    public boolean updateTransactions(Account account, Collection<AggregationTransaction> transactions) {
        if (!updateAccount(account)) {
            return false;
        }
        System.out.println(String.format("Updating transactions for account (%s)", account.getAccountNumber()));
        transactions.stream()
                .map(UpdateControllerTestImpl::toString)
                .forEach(System.out::println);
        return true;
    }

    @Override
    @VisibleForTesting
    public void updateEInvoices(List<Transfer> eInvoices) {
        System.out.println("Updating e-invoices: " + eInvoices);
    }

    @Override
    @VisibleForTesting
    public void updateTransferDestinationPatterns(TransferDestinationsResponse transferDestinations) {
        for (Map.Entry<se.tink.backend.aggregation.rpc.Account, List<TransferDestinationPattern>> entry :
                transferDestinations.getDestinations().entrySet()) {
            System.out.println(String.format("Updating transfer destinations for account(%s)",
                    entry.getKey().getAccountNumber()));
            entry.getValue().forEach(System.out::println);
        }
    }

    @Override
    @VisibleForTesting
    public Collection<se.tink.backend.aggregation.rpc.Account> getAccounts() {
        // Must be rpc.Accounts, because those are 'properly' updated with userId and the like.
        return accounts.stream().map(Account::toSystemAccount).collect(Collectors.toList());
    }

    private static String toString(AggregationTransaction transaction) {
        return MoreObjects.toStringHelper(transaction)
                .add("amount", transaction.getAmount())
                .add("description", transaction.getDescription())
                .add("date", transaction.getDate())
                .add("type", transaction.getType())
                .toString();
    }

    private static String toString(Account account) {
        return MoreObjects.toStringHelper(account)
                .add("type", account.getType())
                .add("name", account.getName())
                .add("accountNumber", account.getAccountNumber())
                .add("balance", account.getBalance())
                .toString();
    }
}
