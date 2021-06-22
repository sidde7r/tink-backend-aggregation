package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base;

import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class FrOpenBankingRequestValidator {

    private final boolean isBnpParibasGroup;

    public FrOpenBankingRequestValidator(String providerName) {
        isBnpParibasGroup =
                providerName.startsWith("fr-bnpparibas-ob")
                        || providerName.startsWith("fr-hellobank-ob");
    }

    public Optional<PaymentException> validateRequestBody(PaymentRequest paymentRequest) {

        boolean amountTooLow =
                paymentRequest
                                .getPayment()
                                .getExactCurrencyAmount()
                                .getExactValue()
                                .compareTo(new BigDecimal("1.0"))
                        < 0;
        if (isBnpParibasGroup && amountTooLow) {
            return Optional.of(
                    new PaymentValidationException(
                            "Transfer amount can't be less than 1.0 EUR.",
                            InternalStatus.INVALID_MINIMUM_AMOUNT));
        } else {

            return Optional.empty();
        }
    }
}
