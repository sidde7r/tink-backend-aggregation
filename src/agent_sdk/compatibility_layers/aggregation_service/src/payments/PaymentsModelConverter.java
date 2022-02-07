package src.agent_sdk.compatibility_layers.aggregation_service.src.payments;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.agent.runtime.models.payments.PaymentImpl;
import se.tink.agent.sdk.models.payments.payment.Creditor;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment.PaymentType;
import se.tink.agent.sdk.models.payments.payment.RemittanceInformation;
import se.tink.agent.sdk.models.payments.payment.RemittanceInformationType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.Transfer;

public final class PaymentsModelConverter {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");

    private PaymentsModelConverter() {
        throw new IllegalStateException("Not reachable.");
    }

    public static List<Payment> mapTransfers(List<Transfer> transfers) {
        return transfers.stream()
                .map(PaymentsModelConverter::mapTransfer)
                .collect(Collectors.toList());
    }

    public static Payment mapTransfer(Transfer transfer) {
        AccountIdentifier destination = transfer.getDestination();
        Amount amount = transfer.getAmount();
        return new PaymentImpl(
                transfer.getId().toString(),
                mapPaymentType(transfer.getPaymentScheme()),
                Optional.ofNullable(transfer.getSource()).map(Debtor::new).orElse(null),
                transfer.getSourceMessage(),
                new Creditor(destination, destination.getName().orElse(null)),
                ExactCurrencyAmount.of(amount.getValue(), amount.getCurrency()),
                mapRemittanceInformation(transfer.getRemittanceInformation()),
                mapExecutionDate(transfer.getDueDate()));
    }

    private static PaymentType mapPaymentType(PaymentScheme paymentScheme) {
        switch (paymentScheme) {
            case SEPA_CREDIT_TRANSFER:
                return PaymentType.SEPA_CREDIT_TRANSFER;
            case SEPA_INSTANT_CREDIT_TRANSFER:
                return PaymentType.SEPA_INSTANT_CREDIT_TRANSFER;
            case FASTER_PAYMENTS:
                return PaymentType.FASTER_PAYMENTS;
            case NORWEGIAN_DOMESTIC_CREDIT_TRANSFER:
                return PaymentType.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER;
            case INSTANT_NORWEGIAN_DOMESTIC_CREDIT_TRANSFER_STRAKS:
                return PaymentType.INSTANT_NORWEGIAN_DOMESTIC_CREDIT_TRANSFER_STRAKS;

            default:
                throw new IllegalStateException("Unexpected value: " + paymentScheme);
        }
    }

    private static RemittanceInformation mapRemittanceInformation(
            se.tink.libraries.transfer.rpc.RemittanceInformation transferRemittanceInformation) {
        RemittanceInformationType remittanceInformationType =
                RemittanceInformationType.valueOf(transferRemittanceInformation.getType().name());
        return new RemittanceInformation(
                remittanceInformationType, transferRemittanceInformation.getValue());
    }

    private static LocalDate mapExecutionDate(Date transferDueDate) {
        return Instant.ofEpochMilli(transferDueDate.getTime())
                .atZone(DEFAULT_ZONE_ID)
                .toLocalDate();
    }
}
