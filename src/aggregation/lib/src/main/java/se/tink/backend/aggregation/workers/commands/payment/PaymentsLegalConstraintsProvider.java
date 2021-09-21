package se.tink.backend.aggregation.workers.commands.payment;

import se.tink.libraries.payments_legal_constraints.PaymentsLegalConstraints;

public class PaymentsLegalConstraintsProvider {

    public PaymentsLegalConstraints getForAppId(String appId) {
        return PaymentsLegalConstraints.get(appId);
    }
}
