package se.tink.backend.aggregation.nxgen.controllers.refresh;

import com.google.common.collect.Sets;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.util.LoanInterpreter;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.user.rpc.User;

public class UpdateController {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected final String currency;
    private final HashSet<Account> accounts = Sets.newHashSet();
    private final LoanInterpreter loanInterpreter;
    protected final User user;
    private final Provider provider;

    public UpdateController(Provider provider, User user) {
        // TODO: Remove when provider uses MarketCode
        MarketCode market = MarketCode.valueOf(provider.getMarket());
        this.loanInterpreter = LoanInterpreter.getInstance(market);
        this.currency = provider.getCurrency();
        this.provider = provider;
        this.user = user;
    }

    public Pair<se.tink.backend.agents.rpc.Account, AccountFeatures> updateAccount(
            Account account) {
        return updateAccount(account, AccountFeatures.createEmpty());
    }

    public Pair<se.tink.backend.agents.rpc.Account, AccountFeatures> updateAccount(
            LoanAccount account) {

        return updateAccount(
                account,
                AccountFeatures.createForLoan(
                        account.getDetails().toSystemLoan(account, loanInterpreter)));
    }

    public Pair<se.tink.backend.agents.rpc.Account, AccountFeatures> updateAccount(
            InvestmentAccount account) {
        return updateAccount(
                account, AccountFeatures.createForPortfolios(account.getSystemPortfolios()));
    }

    private Pair<se.tink.backend.agents.rpc.Account, AccountFeatures> updateAccount(
            Account account, AccountFeatures accountFeatures) {

        if (!FeatureFlags.FeatureFlagGroup.MULTI_CURRENCY_FOR_POCS.isFlagInGroup(user.getFlags())
                && !currency.equalsIgnoreCase(account.getExactBalance().getCurrencyCode())) {
            logger.info(
                    String.format(
                            "Found incompatible Account currencies (expected: %s, but was: %s)",
                            currency, account.getExactBalance().getCurrencyCode()));
            return null;
        }

        if (accounts.contains(account)) {
            logger.warn("Updating an already updated account");
        }

        accounts.add(account);
        return Pair.of(account.toSystemAccount(user, provider), accountFeatures);
    }

    public Pair<se.tink.backend.agents.rpc.Account, List<Transaction>> updateTransactions(
            Account account, Collection<AggregationTransaction> transactions) {

        if (updateAccount(account) == null) {
            return null;
        }
        return Pair.of(
                account.toSystemAccount(user, provider),
                transactions.stream()
                        .map(t -> t.toSystemTransaction(user.isMultiCurrencyEnabled()))
                        .collect(Collectors.toList()));
    }

    public Pair<se.tink.backend.agents.rpc.Account, List<Transaction>> updateTransactions(
            LoanAccount account, Collection<AggregationTransaction> transactions) {

        if (updateAccount(account) == null) {
            return null;
        }

        return Pair.of(
                account.toSystemAccount(user, provider),
                transactions.stream()
                        .map(t -> t.toSystemTransaction(user.isMultiCurrencyEnabled()))
                        .collect(Collectors.toList()));
    }
}
