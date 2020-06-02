package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.psd2.common.Psd2CapabilitiesRule;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.psd2.common.Psd2CheckingAccountRule;

public class PaymentAccountRulesProvider {
    // rules are to be processed identically but are split into two collections to just improve
    // readability
    private static final List<ClassificationRule<PaymentAccountClassification>> psd2Rules =
            new ArrayList<>();
    private static final List<ClassificationRule<PaymentAccountClassification>> marketRules =
            new ArrayList<>();

    private PaymentAccountRulesProvider() {}

    public static List<ClassificationRule<PaymentAccountClassification>> getRules() {
        return Stream.of(psd2Rules, marketRules)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    static {
        psd2Rules.add(new Psd2CapabilitiesRule());
        psd2Rules.add(new Psd2CheckingAccountRule());
    }
}
