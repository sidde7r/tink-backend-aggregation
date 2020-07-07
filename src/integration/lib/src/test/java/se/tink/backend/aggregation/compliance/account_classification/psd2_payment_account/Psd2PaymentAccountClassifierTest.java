package se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.AccountClassificationRule;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;

public class Psd2PaymentAccountClassifierTest {

    private static final boolean ENABLED = true;
    private static final boolean DISABLED = false;
    private static final String DUMMY_BANK_ID = "dummyBankId";

    @Test
    public void shouldClassifyAsPaymentAccountIfRuleReturnsPaymentAccount() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of(
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult
                                        .UNDETERMINED_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                ENABLED, Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult.NON_PAYMENT_ACCOUNT));

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(
                        prepareMockedProvider(), prepareMockedAccountWithUniqueId(DUMMY_BANK_ID));

        // then
        assertThat(result)
                .isEqualTo(Optional.of(Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT));
    }

    @Test
    public void shouldClassifyAsNonPaymentAccountIfRuleReturningPaymentAccountIsDisabled() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of(
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult
                                        .UNDETERMINED_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                DISABLED, Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult.NON_PAYMENT_ACCOUNT));

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(
                        prepareMockedProvider(), prepareMockedAccountWithUniqueId(DUMMY_BANK_ID));

        // then
        assertThat(result)
                .isEqualTo(Optional.of(Psd2PaymentAccountClassificationResult.NON_PAYMENT_ACCOUNT));
    }

    @Test
    public void shouldClassifyAsNonPaymentAccountIfNoRuleReturnsPaymentAccount() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of(
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult
                                        .UNDETERMINED_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult.NON_PAYMENT_ACCOUNT));

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(
                        prepareMockedProvider(), prepareMockedAccountWithUniqueId(DUMMY_BANK_ID));

        // then
        assertThat(result)
                .isEqualTo(Optional.of(Psd2PaymentAccountClassificationResult.NON_PAYMENT_ACCOUNT));
    }

    @Test
    public void shouldClassifyAsUndeterminedIfNoRuleDeterminedDifferently() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of(
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult
                                        .UNDETERMINED_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult
                                        .UNDETERMINED_PAYMENT_ACCOUNT));

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(
                        prepareMockedProvider(), prepareMockedAccountWithUniqueId(DUMMY_BANK_ID));

        // then
        assertThat(result)
                .isEqualTo(
                        Optional.of(
                                Psd2PaymentAccountClassificationResult
                                        .UNDETERMINED_PAYMENT_ACCOUNT));
    }

    @Test
    public void shouldNotClassifyIfNoRules() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of();

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(
                        prepareMockedProvider(), prepareMockedAccountWithUniqueId(DUMMY_BANK_ID));

        // then
        assertThat(result).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldNotClassifyIfNoRulesEnabled() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of(
                        prepareMockedRule(
                                DISABLED,
                                Psd2PaymentAccountClassificationResult
                                        .UNDETERMINED_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                DISABLED, Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                DISABLED,
                                Psd2PaymentAccountClassificationResult.NON_PAYMENT_ACCOUNT));

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(
                        prepareMockedProvider(), prepareMockedAccountWithUniqueId(DUMMY_BANK_ID));

        // then
        assertThat(result).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldOnlyClassifyTwice() {
        // Ensure the caching works by classifying two unique accounts, one of these accounts
        // will be classified twice (should hit the cache!).
        AccountClassificationRule<Psd2PaymentAccountClassificationResult> rule =
                prepareMockedRule(ENABLED, Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT);

        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of(rule);

        Provider provider = prepareMockedProvider();
        Account account1 = prepareMockedAccountWithUniqueId("1");
        Account account2 = prepareMockedAccountWithUniqueId("2");

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // Classify the same account twice.
        classifier.classify(provider, account1);
        classifier.classify(provider, account1);

        // Then classify another account.
        classifier.classify(provider, account2);

        verify(rule, times(2)).classify(Mockito.any(), Mockito.any());
        verify(rule, times(1)).classify(Mockito.any(), eq(account1));
        verify(rule, times(1)).classify(Mockito.any(), eq(account2));
    }

    private AccountClassificationRule<Psd2PaymentAccountClassificationResult> prepareMockedRule(
            boolean isApplicable, Psd2PaymentAccountClassificationResult classificationResult) {
        AccountClassificationRule<Psd2PaymentAccountClassificationResult> rule =
                (AccountClassificationRule<Psd2PaymentAccountClassificationResult>)
                        mock(AccountClassificationRule.class);
        when(rule.isApplicable(any())).thenReturn(isApplicable);
        when(rule.classify(any(), any())).thenReturn(classificationResult);
        return rule;
    }

    private Provider prepareMockedProvider() {
        Provider provider = mock(Provider.class);
        when(provider.getName()).thenReturn("testProvider");
        when(provider.getMarket()).thenReturn("TEST");
        return provider;
    }

    private Account prepareMockedAccountWithUniqueId(String bankId) {
        Account account = mock(Account.class);
        when(account.getType()).thenReturn(AccountTypes.CHECKING);
        when(account.getBankId()).thenReturn(bankId);
        return account;
    }
}
