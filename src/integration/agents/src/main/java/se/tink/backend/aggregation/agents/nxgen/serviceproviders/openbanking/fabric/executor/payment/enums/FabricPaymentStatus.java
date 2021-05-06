package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.libraries.payment.enums.PaymentStatus;

public enum FabricPaymentStatus {
    // AcceptedCustomerProfile. Preceding check of technical validation was successful.
    // Customer profile check was also successful.
    ACCP("ACCP"),
    // AcceptedSettlementCompleted.
    // Settlement on the debtor's account has been completed.
    ACSC("ACSC"),
    // AcceptedSettlementInProcess.
    // All preceding checks such as technical validation and customer profile were successful
    // and therefore the payment initiation has been accepted for execution.
    ACSP("ACSP"),
    // AcceptedTechnicalValidation.
    // Authentication and syntactical and semantical validation are successful.
    ACTC("ACTC"),
    // AcceptedWithChange. Instruction is accepted but a change will be made,
    // such as date or remittance not sent.
    ACWC("ACWC"),
    // AcceptedWithoutPosting. Payment instruction included in the credit
    // transfer is accepted without being posted to the creditor customer’s account.
    ACWP("ACWP"),
    // Received.
    // Payment initiation has been received by the receiving agent.
    RCVD("RCVD"),
    // Rejected. Payment initiation or individual transaction included in
    // the payment initiation has been rejected.
    RJCT("RJCT"),
    // Pending.
    // Payment initiation or individual transaction included in the payment initiation is pending.
    // Further checks and status update will be performed.
    PDNG("PDNG"),
    // Cancelled.
    // Payment initiation has been cancelled before execution.
    CANC("CANC"),

    UNKNOWN("UNKNOWN");

    private String statusText;

    private static final EnumMap<FabricPaymentStatus, PaymentStatus> paymentStatusToTinkMapper =
            new EnumMap<>(FabricPaymentStatus.class);

    private static final EnumMap<FabricPaymentStatus, PaymentStatus>
            paymentStatusToTinkMapperDelete;

    static {
        paymentStatusToTinkMapper.put(ACCP, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACSC, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACSP, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACTC, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACWC, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(ACWP, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(RCVD, PaymentStatus.CREATED);
        paymentStatusToTinkMapper.put(RJCT, PaymentStatus.REJECTED);
        paymentStatusToTinkMapper.put(PDNG, PaymentStatus.SIGNED);
        paymentStatusToTinkMapper.put(CANC, PaymentStatus.CANCELLED);

        paymentStatusToTinkMapperDelete = new EnumMap<>(paymentStatusToTinkMapper);
        paymentStatusToTinkMapperDelete.put(ACTC, PaymentStatus.PENDING);
    }

    FabricPaymentStatus(String status) {
        this.statusText = status;
    }

    public String getText() {
        return this.statusText;
    }

    public static FabricPaymentStatus fromString(String text) {
        return Arrays.stream(FabricPaymentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static PaymentStatus mapToTinkPaymentStatus(FabricPaymentStatus paymentStatus) {
        return Optional.ofNullable(paymentStatusToTinkMapper.get(paymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                FabricConstants.ErrorMessages.MAPPING,
                                                paymentStatus.toString())));
    }

    public static PaymentStatus mapToTinkPaymentStatusDelete(FabricPaymentStatus paymentStatus) {
        return Optional.ofNullable(paymentStatusToTinkMapperDelete.get(paymentStatus))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                FabricConstants.ErrorMessages.MAPPING,
                                                paymentStatus.toString())));
    }

    @Override
    public String toString() {
        return statusText;
    }
}
