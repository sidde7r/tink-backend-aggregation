package se.tink.backend.aggregation.nxgen.controllers.refresh;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.contexts.FinancialDataCacher;
import se.tink.backend.aggregation.constants.MarketCode;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanInterpreter;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.rpc.User;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.aggregation.agents.models.AccountFeatures;

public class UpdateController {
    private static final AggregationLogger log = new AggregationLogger(UpdateController.class);

    private final AgentContext baseContext;
    private final FinancialDataCacher financialDataCacher;
    protected final String currency;
    private final HashSet<Account> accounts = Sets.newHashSet();
    private final LoanInterpreter loanInterpreter;
    protected final User user;

    public UpdateController(AgentContext baseContext, MarketCode market, String currency, User user) {
        this.baseContext = baseContext;
        this.financialDataCacher = baseContext;
        this.loanInterpreter = LoanInterpreter.getInstance(market);
        this.currency = currency;
        this.user = user;
    }

    public boolean updateAccount(Account account) {
        return updateAccount(account, AccountFeatures.createEmpty());
    }

    public boolean updateAccount(LoanAccount account) {
        return updateAccount(account, AccountFeatures.createForLoan(
                account.getDetails().toSystemLoan(account, loanInterpreter)));
    }

    public boolean updateAccount(InvestmentAccount account) {
        return updateAccount(account, AccountFeatures.createForPortfolios(account.getPortfolios()));
    }

    private boolean updateAccount(Account account, AccountFeatures accountFeatures) {

        if (!FeatureFlags.FeatureFlagGroup.MULTI_CURRENCY_FOR_POCS.isFlagInGroup(user.getFlags()) &&
                !currency.equalsIgnoreCase(account.getBalance().getCurrency())) {
            log.info(String.format("Found incompatible Account currencies (expected: %s, but was: %s)",
                    currency, account.getBalance().getCurrency()));
            return false;
        }

        if (accounts.contains(account)) {
            log.warn("Updating an already updated account");
        }

        financialDataCacher.cacheAccount(account.toSystemAccount(user), accountFeatures);

        accounts.add(account);
        return true;
    }

    public boolean updateTransactions(Account account, Collection<AggregationTransaction> transactions) {
        if (!updateAccount(account)) {
            return false;
        }
        financialDataCacher.updateTransactions(account.toSystemAccount(user), transactions.stream()
                .map(t -> t.toSystemTransaction(user))
                .collect(Collectors.toList()));

        return true;
    }

    public boolean updateTransactions(LoanAccount account, Collection<AggregationTransaction> transactions) {
        if (!updateAccount(account)) {
            return false;
        }

        financialDataCacher.cacheTransactions(
                account.toSystemAccount(user).getBankId(),
                transactions
                        .stream()
                        .map(t -> t.toSystemTransaction(user))
                        .collect(Collectors.toList()));

        return true;
    }

    public void updateEInvoices(List<Transfer> eInvoices) {
        baseContext.updateEinvoices(eInvoices);
    }

    public void updateTransferDestinationPatterns(TransferDestinationsResponse transferDestinations) {
        baseContext.updateTransferDestinationPatterns(transferDestinations.getDestinations());
    }

    public Collection<se.tink.backend.agents.rpc.Account> getAccounts() {
        // Must be rpc.Accounts, because those are 'properly' updated with userId and the like.
        return baseContext.getUpdatedAccounts();
    }
}
