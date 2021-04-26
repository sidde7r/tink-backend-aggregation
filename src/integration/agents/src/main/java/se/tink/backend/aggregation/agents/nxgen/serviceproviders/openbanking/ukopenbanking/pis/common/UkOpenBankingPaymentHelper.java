package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.InternalStatus;

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

    public static PaymentType getPaymentType(Payment payment) throws PaymentRejectedException {
        final PaymentType translatedPaymentType =
                PAYMENT_TYPE_MAPPER
                        .translate(payment.getCreditorAndDebtorAccountType())
                        .orElseThrow(
                                () ->
                                        new PaymentRejectedException(
                                                String.format(
                                                        "Cannot map Identifiers, first: %s second: %s",
                                                        Optional.ofNullable(
                                                                        payment
                                                                                .getCreditorAndDebtorAccountType()
                                                                                .first)
                                                                .map(
                                                                        AccountIdentifierType
                                                                                ::toString)
                                                                .orElse(null),
                                                        Optional.ofNullable(
                                                                        payment
                                                                                .getCreditorAndDebtorAccountType()
                                                                                .second)
                                                                .map(
                                                                        AccountIdentifierType
                                                                                ::toString)
                                                                .orElse(null)),
                                                InternalStatus.INVALID_ACCOUNT_TYPE_COMBINATION));

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
