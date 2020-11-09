package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.TypePair;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;

public enum DnbPaymentType {
    SEPA_CREDIT_TRANSFERS("sepa-credit-transfers", PaymentType.SEPA),
    NORWEGIAN_DOMESTIC_CREDIT_TRANSFERS(
            "norwegian-domestic-credit-transfers", PaymentType.DOMESTIC),
    NORWEGIAN_CROSS_BORDER_CREDIT_TRANSFERS(
            "norwegian-cross-border-credit-transfers", PaymentType.INTERNATIONAL),
    UNDEFINED("Undefined", PaymentType.UNDEFINED);

    // We map the payee and recipment account type identifier
    // The transfer cannot be made between different types (ex. IBAN, NO)
    private static final GenericTypeMapper<DnbPaymentType, TypePair>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper.<DnbPaymentType, TypePair>genericBuilder()
                            .put(
                                    NORWEGIAN_DOMESTIC_CREDIT_TRANSFERS,
                                    new TypePair(Type.NO, Type.NO))
                            .put(SEPA_CREDIT_TRANSFERS, new TypePair(Type.IBAN, Type.IBAN))
                            .build();
    private String text;
    private PaymentType paymentType;

    DnbPaymentType(String text, PaymentType paymentType) {
        this.text = text;
        this.paymentType = paymentType;
    }

    public static DnbPaymentType getDnbPaymentType(PaymentRequest paymentRequest) {
        Pair<Type, Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentTypeMapper
                .translate(new TypePair(accountIdentifiersKey))
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        "No DnbPaymentType found for your AccountIdentifiers pair "
                                                + accountIdentifiersKey));
    }

    @Override
    public String toString() {
        return text;
    }

    public PaymentType getTinkPaymentType() {
        return paymentType;
    }
}
