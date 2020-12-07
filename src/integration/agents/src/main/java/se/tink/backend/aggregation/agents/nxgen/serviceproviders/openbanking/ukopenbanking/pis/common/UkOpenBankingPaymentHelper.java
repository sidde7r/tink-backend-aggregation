package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class UkOpenBankingPaymentHelper {

    // TODO: add all possible permutations
    @SuppressWarnings("unchecked")
    private static final GenericTypeMapper<
                    PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
            PAYMENT_TYPE_MAPPER =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                    genericBuilder()
                            .put(
                                    PaymentType.DOMESTIC,
                                    new Pair<>(null, AccountIdentifier.Type.SORT_CODE),
                                    new Pair<>(
                                            AccountIdentifier.Type.SORT_CODE,
                                            AccountIdentifier.Type.SORT_CODE),
                                    new Pair<>(
                                            AccountIdentifier.Type.PAYM_PHONE_NUMBER,
                                            AccountIdentifier.Type.PAYM_PHONE_NUMBER))
                            .put(
                                    PaymentType.SEPA,
                                    new Pair<>(null, AccountIdentifier.Type.IBAN),
                                    new Pair<>(
                                            AccountIdentifier.Type.IBAN,
                                            AccountIdentifier.Type.IBAN))
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
