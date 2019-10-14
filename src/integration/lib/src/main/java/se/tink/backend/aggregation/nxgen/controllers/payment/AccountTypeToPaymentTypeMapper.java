package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.time.LocalDate;
import se.tink.backend.aggregation.constants.MarketCode;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class AccountTypeToPaymentTypeMapper {

    private static GenericTypeMapper<
                    PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
            mapper_se =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                    genericBuilder()
                            .put(
                                    PaymentType.DOMESTIC,
                                    new Pair<>(
                                            AccountIdentifier.Type.SE, AccountIdentifier.Type.SE),
                                    new Pair<>(
                                            AccountIdentifier.Type.BBAN,
                                            AccountIdentifier.Type.BBAN),
                                    new Pair<>(
                                            AccountIdentifier.Type.BBAN,
                                            AccountIdentifier.Type.IBAN),
                                    new Pair<>(
                                            AccountIdentifier.Type.IBAN,
                                            AccountIdentifier.Type.IBAN))
                            .build();

    private static GenericTypeMapper<
                    PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
            mapper_gb =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                    genericBuilder()
                            .put(
                                    PaymentType.DOMESTIC,
                                    new Pair<>(
                                            AccountIdentifier.Type.SORT_CODE,
                                            AccountIdentifier.Type.SORT_CODE),
                                    new Pair<>(
                                            AccountIdentifier.Type.PAYM_PHONE_NUMBER,
                                            AccountIdentifier.Type.PAYM_PHONE_NUMBER))
                            .put(
                                    PaymentType.INTERNATIONAL,
                                    new Pair<>(
                                            AccountIdentifier.Type.IBAN,
                                            AccountIdentifier.Type.IBAN))
                            .build();

    public static PaymentType getType(Payment payment, String marketCode) {
        GenericTypeMapper<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                mapper =
                        GenericTypeMapper
                                .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                        genericBuilder()
                                .build();

        if (String.valueOf(MarketCode.SE).equals(marketCode)) {
            mapper = mapper_se;
        }
        if (String.valueOf(MarketCode.GB).equals(marketCode)) {
            mapper = mapper_gb;
        }
        PaymentType paymentType =
                mapper.translate(payment.getCreditorAndDebtorAccountType())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "Cannot map Identifiers, first: %s second: %s",
                                                        payment.getCreditorAndDebtorAccountType()
                                                                .first
                                                                .toString(),
                                                        payment.getCreditorAndDebtorAccountType()
                                                                .second
                                                                .toString())));

        return checkFutureDate(payment, paymentType);
    }

    private static PaymentType checkFutureDate(Payment payment, PaymentType paymentType) {
        if (!payment.getExecutionDate().isEqual(LocalDate.now())) {
            switch (paymentType) {
                case DOMESTIC:
                    return PaymentType.DOMESTIC_FUTURE;
                case INTERNATIONAL:
                    return PaymentType.INTERNATIONAL_FUTURE;
                default:
            }
        }

        if (paymentType.equals(PaymentType.UNDEFINED)) {
            throw new IllegalStateException(String.format("Unknown type: %s", paymentType));
        }
        return paymentType;
    }
}
