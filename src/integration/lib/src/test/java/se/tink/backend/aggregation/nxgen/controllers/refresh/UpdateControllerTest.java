package se.tink.backend.aggregation.nxgen.controllers.refresh;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.assertj.core.util.Preconditions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.user.rpc.User;

@RunWith(MockitoJUnitRunner.class)
public final class UpdateControllerTest {
    @Spy private FakeAgentContext context;
    @Mock private Provider provider;

    private User user = new User();

    @Test
    public void ensureLoansRemain_whenTransactions_areRefreshed() {
        Mockito.when(provider.getMarket()).thenReturn(MarketCode.SE.name());
        Mockito.when(provider.getCurrency()).thenReturn("SEK");

        final UpdateController updateController = new UpdateController(provider, user);

        final LoanAccount loanAccount =
                LoanAccount.builder("1337")
                        .setAccountNumber("777")
                        .setExactBalance(ExactCurrencyAmount.inSEK(-7.0))
                        .build();

        final Collection<AggregationTransaction> transactions = Collections.emptySet();

        context.accountFeatures = AccountFeatures.createForLoan(new Loan());

        // This call should never discard any cached loans
        updateController.updateTransactions(loanAccount, transactions);

        final String uniqueAccountId = loanAccount.toSystemAccount(user, provider).getBankId();

        Assert.assertTrue(context.getAccountFeatures(uniqueAccountId).isPresent());
        Assert.assertFalse(context.getAccountFeatures(uniqueAccountId).get().getLoans().isEmpty());
    }

    @Test
    public void ensureCacheTransactions_whenUpdateTransactions_isNotCalledWithNull() {
        Mockito.when(provider.getMarket()).thenReturn(MarketCode.SE.name());
        Mockito.when(provider.getCurrency()).thenReturn("SEK");
        final UpdateController updateController = new UpdateController(provider, user);

        final LoanAccount loanAccount =
                LoanAccount.builder("1337")
                        .setAccountNumber("777")
                        .setExactBalance(ExactCurrencyAmount.of(-7.0, "SEK"))
                        .build();

        final Collection<AggregationTransaction> transactions = Collections.emptySet();

        context.accountFeatures = AccountFeatures.createForLoan(new Loan());

        // Must not throw NPE
        updateController.updateTransactions(loanAccount, transactions);
    }

    abstract static class FakeAgentContext extends AgentContext {
        AccountFeatures accountFeatures;

        @Override
        public void cacheAccount(final Account account, final AccountFeatures accountFeatures) {
            this.accountFeatures = accountFeatures;
        }

        @Override
        public void cacheTransactions(
                @Nonnull String accountUniqueId, List<Transaction> transactions) {
            Preconditions.checkNotNull(
                    accountUniqueId); // Necessary until we make @Nonnull throw the exception
        }

        public Optional<AccountFeatures> getAccountFeatures(final String uniqueAccountId) {
            return Optional.ofNullable(accountFeatures);
        }
    }
}
