package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.global.GlobalCheckingAccountRule;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.regulatory.psd2.Psd2Rule;

public class PaymentAccountRulesProvider {
    // rules are to be processed identically but are split into two collections to just improve
    // readability
    private static final List<ClassificationRule<PaymentAccountClassification>> globalRules =
            new ArrayList<>();
    private static final List<ClassificationRule<PaymentAccountClassification>> regulatoryRules =
            new ArrayList<>();
    private static final List<ClassificationRule<PaymentAccountClassification>> marketRules =
            new ArrayList<>();

    private PaymentAccountRulesProvider() {}

    public static List<ClassificationRule<PaymentAccountClassification>> getRules() {
        return Stream.of(globalRules, regulatoryRules, marketRules)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    static {
        globalRules.add(new GlobalCheckingAccountRule());
        regulatoryRules.add(new Psd2Rule());
    }
}
