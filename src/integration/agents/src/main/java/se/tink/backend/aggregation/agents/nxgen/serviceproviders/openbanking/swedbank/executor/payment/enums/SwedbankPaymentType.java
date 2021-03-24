package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.util.AccountTypePair;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.PaymentType;

public enum SwedbankPaymentType {
    SE_DOMESTIC_CREDIT_TRANSFERS("se-domestic-credit-transfers", PaymentType.DOMESTIC),
    SE_INTERNATIONAL_CREDIT_TRANSFERS(
            "se-international-credit-transfers", PaymentType.INTERNATIONAL),
    UNDEFINED("undefined", PaymentType.UNDEFINED);

    private String text;
    private PaymentType paymentType;

    SwedbankPaymentType(String text, PaymentType paymentType) {
        this.text = text;
        this.paymentType = paymentType;
    }

    public PaymentType getTinkPaymentType() {
        return paymentType;
    }

    @Override
    public String toString() {
        return text;
    }

    public static SwedbankPaymentType getPaymentType(AccountTypePair accountTypePair) {
        return accountIdentifiersToPaymentTypeMapper
                .translate(accountTypePair)
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        String.format(
                                                ErrorMessages.INVALID_PAYMENT_TYPE,
                                                accountTypePair)));
    }

    private static final GenericTypeMapper<SwedbankPaymentType, AccountTypePair>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper.<SwedbankPaymentType, AccountTypePair>genericBuilder()
                            .put(
                                    SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS,
                                    new AccountTypePair(
                                            AccountIdentifierType.SE, AccountIdentifierType.SE),
                                    new AccountTypePair(
                                            AccountIdentifierType.SE, AccountIdentifierType.IBAN),
                                    new AccountTypePair(
                                            AccountIdentifierType.SE, AccountIdentifierType.SE_BG),
                                    new AccountTypePair(
                                            AccountIdentifierType.SE, AccountIdentifierType.SE_PG))
                            .put(
                                    SwedbankPaymentType.SE_INTERNATIONAL_CREDIT_TRANSFERS,
                                    new AccountTypePair(
                                            AccountIdentifierType.IBAN, AccountIdentifierType.IBAN))
                            .build();
}
