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
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.constants.MarketCode;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.Transaction;

@RunWith(MockitoJUnitRunner.class)
public final class UpdateControllerTest {
    @Spy
    private FakeAgentContext context;

    private User user = new User();

    private Credentials getCredential() {
        Credentials credentials = new Credentials();
        credentials.setProviderName("Test");
        return credentials;
    }

    @Test
    public void ensureLoansRemain_whenTransactions_areRefreshed() {
        final UpdateController updateController = new UpdateController(context, MarketCode.SE, "SEK", user);

        final LoanAccount loanAccount = LoanAccount.builder("1337")
                .setAccountNumber("777")
                .setBalance(new Amount("SEK", -7.0))
                .build();

        final Collection<AggregationTransaction> transactions = Collections.emptySet();

        context.accountFeatures = AccountFeatures.createForLoan(new Loan());

        // This call should never discard any cached loans
        updateController.updateTransactions(loanAccount, transactions);

        final String uniqueAccountId = loanAccount.toSystemAccount(user).getBankId();

        Assert.assertTrue(context.getAccountFeatures(uniqueAccountId).isPresent());
        Assert.assertFalse(context.getAccountFeatures(uniqueAccountId).get().getLoans().isEmpty());
    }

    @Test
    public void ensureCacheTransactions_whenUpdateTransactions_isNotCalledWithNull() {
        final UpdateController updateController = new UpdateController(context, MarketCode.SE, "SEK", user);

        final LoanAccount loanAccount = LoanAccount.builder("1337")
                .setAccountNumber("777")
                .setBalance(new Amount("SEK", -7.0))
                .build();

        final Collection<AggregationTransaction> transactions = Collections.emptySet();

        context.accountFeatures = AccountFeatures.createForLoan(new Loan());

        // Must not throw NPE
        updateController.updateTransactions(loanAccount, transactions);
    }

    static abstract class FakeAgentContext extends AgentContext {
        AccountFeatures accountFeatures;

        @Override
        public void cacheAccount(final se.tink.backend.aggregation.rpc.Account account,
                final AccountFeatures accountFeatures) {
            this.accountFeatures = accountFeatures;
        }

        @Override
        public void cacheTransactions(@Nonnull String accountUniqueId, List<Transaction> transactions) {
            Preconditions.checkNotNull(accountUniqueId); // Necessary until we make @Nonnull throw the exception
        }

        public Optional<AccountFeatures> getAccountFeatures(final String uniqueAccountId) {
            return Optional.ofNullable(accountFeatures);
        }

    }
}
