package se.tink.backend.aggregation.nxgen.controllers.refresh;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
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
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Loan;

@RunWith(MockitoJUnitRunner.class)
public final class UpdateControllerTest {
    @Spy
    private FakeAgentContext context;

    private Credentials getCredential() {
        Credentials credentials = new Credentials();
        credentials.setProviderName("Test");
        return credentials;
    }

    @Test
    public void ensureLoansRemain_whenTransactions_areRefreshed() {
        final UpdateController updateController = new UpdateController(context, MarketCode.SE, "SEK", getCredential());

        final LoanAccount loanAccount = LoanAccount.builder("1337")
                .setAccountNumber("777")
                .setBalance(new Amount("SEK", -7.0))
                .build();

        final Collection<AggregationTransaction> transactions = Collections.emptySet();

        context.accountFeatures = AccountFeatures.createForLoan(new Loan());

        // This call should never discard any cached loans
        updateController.updateTransactions(loanAccount, transactions);

        final String uniqueAccountId = loanAccount.toSystemAccount().getBankId();

        Assert.assertTrue(context.getAccountFeatures(uniqueAccountId).isPresent());
        Assert.assertFalse(context.getAccountFeatures(uniqueAccountId).get().getLoans().isEmpty());
    }

    static abstract class FakeAgentContext extends AgentContext {
        AccountFeatures accountFeatures;

        @Override
        public void cacheAccount(final se.tink.backend.aggregation.rpc.Account account,
                final AccountFeatures accountFeatures) {
            this.accountFeatures = accountFeatures;
        }

        @Override
        public Optional<AccountFeatures> getAccountFeatures(final String uniqueAccountId) {
            return Optional.ofNullable(accountFeatures);
        }
    }
}
