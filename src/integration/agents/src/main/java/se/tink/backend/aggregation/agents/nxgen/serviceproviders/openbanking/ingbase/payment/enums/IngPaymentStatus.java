package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.enums;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.libraries.payment.enums.PaymentStatus;

@Getter
@RequiredArgsConstructor
public enum IngPaymentStatus {

    // common
    RECEIVED("RCVD", PaymentStatus.USER_APPROVAL_FAILED),
    ACCEPTED_TECHNICAL_VALIDATION("ACTC", PaymentStatus.PENDING),
    PENDING("PDNG", PaymentStatus.PENDING),
    ACCEPTED_CUSTOMER_PROFILE("ACCP", PaymentStatus.SIGNED),
    ACCEPTED_WITH_CHANGE("ACWC", PaymentStatus.SIGNED),
    ACCEPTED_SETTLEMENT_IN_PROCESS("ACSP", PaymentStatus.SIGNED),
    PARTIALLY_ACCEPTED("PART", PaymentStatus.PENDING),
    PARTIALLY_ACCEPTED_TECHNICAL("PATC", PaymentStatus.PENDING),
    ACCEPTED_SETTLEMENT_COMPLETED_ON_DEBTOR_ACCOUNT("ACSC", PaymentStatus.PAID),
    CANCELED("CANC", PaymentStatus.CANCELLED),
    REJECTED("RJCT", PaymentStatus.REJECTED),
    // recurring
    ACTIVE("ACTV", PaymentStatus.SIGNED),
    EXPIRED("EXPI", PaymentStatus.SIGNED),
    // other
    UNKNOWN("Unknown", PaymentStatus.UNDEFINED);

    private final String apiValue;
    private final PaymentStatus tinkPaymentStatus;

    public static IngPaymentStatus fromTransactionStatus(String transactionStatus) {
        return Arrays.stream(IngPaymentStatus.values())
                .filter(paymentStatus -> paymentStatus.apiValue.equalsIgnoreCase(transactionStatus))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
