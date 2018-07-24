package se.tink.backend.aggregation.nxgen.controllers.refresh;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.rpc.AccountFeatures;

public class UpdateController {
    private static final AggregationLogger log = new AggregationLogger(UpdateController.class);

    private final AgentContext baseContext;
    private final Credentials credentials;
    protected final String currency;
    private final HashSet<Account> accounts = Sets.newHashSet();

    public UpdateController(AgentContext baseContext, Credentials credentials, String currency) {
        this.baseContext = baseContext;
        this.credentials = credentials;
        this.currency = currency;
    }

    public boolean updateAccount(Account account) {
        return updateAccount(account, AccountFeatures.createEmpty());
    }

    public boolean updateAccount(LoanAccount account) {
        return updateAccount(account, AccountFeatures.createForLoan(account.getDetails().toSystemLoan(account)));
    }

    public boolean updateAccount(InvestmentAccount account) {
        return updateAccount(account, AccountFeatures.createForPortfolios(account.getPortfolios()));
    }

    private boolean updateAccount(Account account, AccountFeatures accountFeatures) {
        if (!currency.equalsIgnoreCase(account.getBalance().getCurrency())) {
            log.info(String.format("Found incompatible Account currencies (expected: %s, but was: %s)",
                    currency, account.getBalance().getCurrency()));
            return false;
        }
        if (accounts.contains(account)) {
            log.warn("Updating an already updated account");
        }

        baseContext.cacheAccount(account.toSystemAccount(), accountFeatures);

        accounts.add(account);
        return true;
    }

    public boolean updateTransactions(Account account, Collection<AggregationTransaction> transactions) {
        if (!updateAccount(account)) {
            return false;
        }

        baseContext.updateTransactions(account.toSystemAccount(), transactions.stream()
                .map(AggregationTransaction::toSystemTransaction)
                .collect(Collectors.toList()));

        return true;
    }

    public void updateEInvoices(List<Transfer> eInvoices) {
        baseContext.updateEinvoices(eInvoices);
    }

    public void updateTransferDestinationPatterns(TransferDestinationsResponse transferDestinations) {
        baseContext.updateTransferDestinationPatterns(transferDestinations.getDestinations());
    }

    public Collection<se.tink.backend.aggregation.rpc.Account> getAccounts() {
        // Must be rpc.Accounts, because those are 'properly' updated with userId and the like.
        return baseContext.getUpdatedAccounts();
    }
}
