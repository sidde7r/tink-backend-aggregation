package se.tink.backend.aggregation.nxgen.controllers.refresh;

import com.google.common.collect.Sets;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.constants.MarketCode;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanInterpreter;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.user.rpc.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateController {
    private static final AggregationLogger log = new AggregationLogger(UpdateController.class);

    protected final String currency;
    private final HashSet<Account> accounts = Sets.newHashSet();
    private final LoanInterpreter loanInterpreter;
    protected final User user;

    public UpdateController(MarketCode market, String currency, User user) {
        this.loanInterpreter = LoanInterpreter.getInstance(market);
        this.currency = currency;
        this.user = user;
    }

    public Pair<se.tink.backend.agents.rpc.Account, AccountFeatures> updateAccount(Account account) {
        return updateAccount(account, AccountFeatures.createEmpty());
    }

    public Pair<se.tink.backend.agents.rpc.Account, AccountFeatures> updateAccount(LoanAccount account) {

        return updateAccount(account, AccountFeatures.createForLoan(
                account.getDetails().toSystemLoan(account, loanInterpreter)));
    }


    public Pair<se.tink.backend.agents.rpc.Account, AccountFeatures> updateAccount(InvestmentAccount account) {
        return updateAccount(account, AccountFeatures.createForPortfolios(account.getPortfolios()));
    }

    private Pair<se.tink.backend.agents.rpc.Account, AccountFeatures> updateAccount(Account account, AccountFeatures accountFeatures) {


        if (!FeatureFlags.FeatureFlagGroup.MULTI_CURRENCY_FOR_POCS.isFlagInGroup(user.getFlags()) &&
                !currency.equalsIgnoreCase(account.getBalance().getCurrency())) {
            log.info(String.format("Found incompatible Account currencies (expected: %s, but was: %s)",
                    currency, account.getBalance().getCurrency()));
            return null;
        }

        if (accounts.contains(account)) {
            log.warn("Updating an already updated account");
        }


        accounts.add(account);
        return Pair.of(account.toSystemAccount(user), accountFeatures);
    }


    public Pair<se.tink.backend.agents.rpc.Account, List<Transaction>> updateTransactions(Account account, Collection<AggregationTransaction> transactions) {


        if (updateAccount(account) == null) {
            return null;
        }
        return Pair.of(account.toSystemAccount(user), transactions.stream()
                .map(t -> t.toSystemTransaction(user))
                .collect(Collectors.toList()));

    }

    public Pair<se.tink.backend.agents.rpc.Account, List<Transaction>> updateTransactions(LoanAccount account, Collection<AggregationTransaction> transactions) {

        if (updateAccount(account) == null) {
            return null;
        }

        return Pair.of(
                account.toSystemAccount(user),
                transactions
                        .stream()
                        .map(t -> t.toSystemTransaction(user))
                        .collect(Collectors.toList()));
    }
}
