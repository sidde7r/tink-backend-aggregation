package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class PaymentAccountClassifierTest {

    private static final boolean ENABLED = true;
    private static final boolean DISABLED = false;
    private MetricRegistry metricRegistry = new MetricRegistry();

    @Test
    public void shouldClassifyAsPaymentAccountIfRuleReturnsPaymentAccount() {
        List<ClassificationRule<PaymentAccountClassification>> rules = new ArrayList<>();
        rules.add(prepareMockedRule(ENABLED, PaymentAccountClassification.UNDETERMINED));
        rules.add(prepareMockedRule(ENABLED, PaymentAccountClassification.PAYMENT_ACCOUNT));
        rules.add(prepareMockedRule(ENABLED, PaymentAccountClassification.NON_PAYMENT_ACCOUNT));
        PaymentAccountClassifier classifier =
                new PaymentAccountClassifier(rules, metricRegistry, prepareMockedProvider());

        // when
        PaymentAccountClassification result =
                classifier.classifyAsPaymentAccount(prepareMockedAccount());

        // then
        assertThat(result).isEqualTo(PaymentAccountClassification.PAYMENT_ACCOUNT);
    }

    @Test
    public void shouldClassifyAsNonPaymentAccountIfRuleReturningPaymentAccountIsDisabled() {
        List<ClassificationRule<PaymentAccountClassification>> rules = new ArrayList<>();
        rules.add(prepareMockedRule(ENABLED, PaymentAccountClassification.UNDETERMINED));
        rules.add(prepareMockedRule(DISABLED, PaymentAccountClassification.PAYMENT_ACCOUNT));
        rules.add(prepareMockedRule(ENABLED, PaymentAccountClassification.NON_PAYMENT_ACCOUNT));
        PaymentAccountClassifier classifier =
                new PaymentAccountClassifier(rules, metricRegistry, prepareMockedProvider());

        // when
        PaymentAccountClassification result =
                classifier.classifyAsPaymentAccount(prepareMockedAccount());

        // then
        assertThat(result).isEqualTo(PaymentAccountClassification.NON_PAYMENT_ACCOUNT);
    }

    @Test
    public void shouldClassifyAsNonPaymentAccountIfNoRuleReturnsPaymentAccount() {
        List<ClassificationRule<PaymentAccountClassification>> rules = new ArrayList<>();
        rules.add(prepareMockedRule(ENABLED, PaymentAccountClassification.UNDETERMINED));
        rules.add(prepareMockedRule(ENABLED, PaymentAccountClassification.NON_PAYMENT_ACCOUNT));
        PaymentAccountClassifier classifier =
                new PaymentAccountClassifier(rules, metricRegistry, prepareMockedProvider());

        // when
        PaymentAccountClassification result =
                classifier.classifyAsPaymentAccount(prepareMockedAccount());

        // then
        assertThat(result).isEqualTo(PaymentAccountClassification.NON_PAYMENT_ACCOUNT);
    }

    @Test
    public void shouldClassifyAsUndeterminedIfNoRuleDeterminedDifferently() {
        List<ClassificationRule<PaymentAccountClassification>> rules = new ArrayList<>();
        rules.add(prepareMockedRule(ENABLED, PaymentAccountClassification.UNDETERMINED));
        rules.add(prepareMockedRule(ENABLED, PaymentAccountClassification.UNDETERMINED));
        PaymentAccountClassifier classifier =
                new PaymentAccountClassifier(rules, metricRegistry, prepareMockedProvider());

        // when
        PaymentAccountClassification result =
                classifier.classifyAsPaymentAccount(prepareMockedAccount());

        // then
        assertThat(result).isEqualTo(PaymentAccountClassification.UNDETERMINED);
    }

    @Test
    public void shouldClassifyAsUndeterminedIfNoRules() {
        PaymentAccountClassifier classifier =
                new PaymentAccountClassifier(
                        Collections.emptyList(), metricRegistry, prepareMockedProvider());

        // when
        PaymentAccountClassification result =
                classifier.classifyAsPaymentAccount(prepareMockedAccount());

        // then
        assertThat(result).isEqualTo(PaymentAccountClassification.UNDETERMINED);
    }

    @Test
    public void shouldClassifyAsUndeterminedIfNoRulesEnabled() {
        List<ClassificationRule<PaymentAccountClassification>> rules = new ArrayList<>();
        rules.add(prepareMockedRule(DISABLED, PaymentAccountClassification.UNDETERMINED));
        rules.add(prepareMockedRule(DISABLED, PaymentAccountClassification.PAYMENT_ACCOUNT));
        rules.add(prepareMockedRule(DISABLED, PaymentAccountClassification.NON_PAYMENT_ACCOUNT));
        PaymentAccountClassifier classifier =
                new PaymentAccountClassifier(rules, metricRegistry, prepareMockedProvider());

        // when
        PaymentAccountClassification result =
                classifier.classifyAsPaymentAccount(prepareMockedAccount());

        // then
        assertThat(result).isEqualTo(PaymentAccountClassification.UNDETERMINED);
    }

    private ClassificationRule<PaymentAccountClassification> prepareMockedRule(
            boolean isApplicable, PaymentAccountClassification classificationResult) {
        ClassificationRule<PaymentAccountClassification> rule =
                (ClassificationRule<PaymentAccountClassification>) mock(ClassificationRule.class);
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
