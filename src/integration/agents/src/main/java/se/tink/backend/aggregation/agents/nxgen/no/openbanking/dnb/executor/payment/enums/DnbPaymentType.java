package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.util.TypePair;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;

public enum DnbPaymentType {
    NorwegianCrossBorderCreditTransfers(
            "norwegian-cross-border-credit-transfers", PaymentType.INTERNATIONAL),
    NorwegianDomesticCreditTransfers("norwegian-domestic-credit-transfers", PaymentType.DOMESTIC),
    NorwegianDomesticPaymentToSelf("norwegian-domestic-payment-to-self", PaymentType.DOMESTIC),
    Undefined("Undefined", PaymentType.UNDEFINED);

    private String text;
    private PaymentType paymentType;

    DnbPaymentType(String text, PaymentType paymentType) {
        this.text = text;
        this.paymentType = paymentType;
    }

    private static final GenericTypeMapper<DnbPaymentType, TypePair>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper.<DnbPaymentType, TypePair>genericBuilder()
                            .put(NorwegianDomesticCreditTransfers, new TypePair(Type.NO, Type.NO))
                            .put(
                                    NorwegianCrossBorderCreditTransfers,
                                    new TypePair(Type.IBAN, Type.IBAN),
                                    new TypePair(Type.NO, Type.IBAN))
                            .build();

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
