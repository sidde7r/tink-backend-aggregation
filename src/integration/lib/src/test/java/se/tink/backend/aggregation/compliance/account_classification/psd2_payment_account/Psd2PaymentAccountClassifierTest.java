package se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.AccountClassificationRule;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;

public class Psd2PaymentAccountClassifierTest {

    private static final boolean ENABLED = true;
    private static final boolean DISABLED = false;

    @Test
    public void shouldClassifyAsPaymentAccountIfRuleReturnsPaymentAccount() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of(
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult
                                        .PSD2_UNDETERMINED_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult.PSD2_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult.PSD2_NON_PAYMENT_ACCOUNT));

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(prepareMockedProvider(), prepareMockedAccount());

        // then
        assertThat(result)
                .isEqualTo(
                        Optional.of(Psd2PaymentAccountClassificationResult.PSD2_PAYMENT_ACCOUNT));
    }

    @Test
    public void shouldClassifyAsNonPaymentAccountIfRuleReturningPaymentAccountIsDisabled() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of(
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult
                                        .PSD2_UNDETERMINED_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                DISABLED,
                                Psd2PaymentAccountClassificationResult.PSD2_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult.PSD2_NON_PAYMENT_ACCOUNT));

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(prepareMockedProvider(), prepareMockedAccount());

        // then
        assertThat(result)
                .isEqualTo(
                        Optional.of(
                                Psd2PaymentAccountClassificationResult.PSD2_NON_PAYMENT_ACCOUNT));
    }

    @Test
    public void shouldClassifyAsNonPaymentAccountIfNoRuleReturnsPaymentAccount() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of(
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult
                                        .PSD2_UNDETERMINED_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult.PSD2_NON_PAYMENT_ACCOUNT));

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(prepareMockedProvider(), prepareMockedAccount());

        // then
        assertThat(result)
                .isEqualTo(
                        Optional.of(
                                Psd2PaymentAccountClassificationResult.PSD2_NON_PAYMENT_ACCOUNT));
    }

    @Test
    public void shouldClassifyAsUndeterminedIfNoRuleDeterminedDifferently() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of(
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult
                                        .PSD2_UNDETERMINED_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                ENABLED,
                                Psd2PaymentAccountClassificationResult
                                        .PSD2_UNDETERMINED_PAYMENT_ACCOUNT));

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(prepareMockedProvider(), prepareMockedAccount());

        // then
        assertThat(result)
                .isEqualTo(
                        Optional.of(
                                Psd2PaymentAccountClassificationResult
                                        .PSD2_UNDETERMINED_PAYMENT_ACCOUNT));
    }

    @Test
    public void shouldNotClassifyIfNoRules() {
        ImmutableList<AccountClassificationRule<Psd2PaymentAccountClassificationResult>> rules =
                ImmutableList.of();

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(prepareMockedProvider(), prepareMockedAccount());

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
                                        .PSD2_UNDETERMINED_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                DISABLED,
                                Psd2PaymentAccountClassificationResult.PSD2_PAYMENT_ACCOUNT),
                        prepareMockedRule(
                                DISABLED,
                                Psd2PaymentAccountClassificationResult.PSD2_NON_PAYMENT_ACCOUNT));

        Psd2PaymentAccountClassifier classifier =
                Psd2PaymentAccountClassifier.createWithRules(rules);

        // when
        Optional<Psd2PaymentAccountClassificationResult> result =
                classifier.classify(prepareMockedProvider(), prepareMockedAccount());

        // then
        assertThat(result).isEqualTo(Optional.empty());
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

    private Account prepareMockedAccount() {
        Account account = mock(Account.class);
        when(account.getType()).thenReturn(AccountTypes.CHECKING);
        return account;
    }
}
