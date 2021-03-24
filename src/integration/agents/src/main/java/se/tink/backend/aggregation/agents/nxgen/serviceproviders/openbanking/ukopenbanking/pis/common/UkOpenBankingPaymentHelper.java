package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class UkOpenBankingPaymentHelper {

    // TODO: add all possible permutations
    @SuppressWarnings("unchecked")
    private static final GenericTypeMapper<
                    PaymentType, Pair<AccountIdentifierType, AccountIdentifierType>>
            PAYMENT_TYPE_MAPPER =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifierType, AccountIdentifierType>>
                                    genericBuilder()
                            .put(
                                    PaymentType.DOMESTIC,
                                    new Pair<>(null, AccountIdentifierType.SORT_CODE),
                                    new Pair<>(
                                            AccountIdentifierType.SORT_CODE,
                                            AccountIdentifierType.SORT_CODE),
                                    new Pair<>(
                                            AccountIdentifierType.PAYM_PHONE_NUMBER,
                                            AccountIdentifierType.PAYM_PHONE_NUMBER))
                            .put(
                                    PaymentType.SEPA,
                                    new Pair<>(null, AccountIdentifierType.IBAN),
                                    new Pair<>(
                                            AccountIdentifierType.IBAN, AccountIdentifierType.IBAN))
                            .build();

    public static PaymentType getPaymentType(Payment payment) {
        final PaymentType translatedPaymentType =
                PAYMENT_TYPE_MAPPER
                        .translate(payment.getCreditorAndDebtorAccountType())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                String.format(
                                                        "Cannot map Identifiers, first: %s second: %s",
                                                        payment.getCreditorAndDebtorAccountType()
                                                                .first
                                                                .toString(),
                                                        payment.getCreditorAndDebtorAccountType()
                                                                .second
                                                                .toString())));

        if (isFutureDatePayment(payment)) {
            return PaymentType.DOMESTIC_FUTURE;
        }

        return translatedPaymentType;
    }

    private static boolean isFutureDatePayment(Payment payment) {
        final LocalDate currentDate = LocalDate.now(Clock.systemUTC());

        return Objects.nonNull(payment.getExecutionDate())
                && payment.getExecutionDate().isAfter(currentDate);
    }
}
