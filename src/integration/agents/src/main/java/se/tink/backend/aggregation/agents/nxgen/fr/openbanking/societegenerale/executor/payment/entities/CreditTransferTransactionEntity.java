package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@JsonObject
public class CreditTransferTransactionEntity {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    private PaymentId paymentId;
    private String requestedExecutionDate;
    private InstructedAmountEntity instructedAmount;
    private RemittanceInformationEntity remittanceInformation;

    @JsonIgnore
    public static List<CreditTransferTransactionEntity> of(PaymentRequest paymentRequest) {
        InstructedAmountEntity instructedAmount = InstructedAmountEntity.of(paymentRequest);
        List<CreditTransferTransactionEntity> transactions = new ArrayList<>();
        List<String> remittanceInformation = new ArrayList<>();
        PaymentId paymentId = new PaymentId();
        Payment payment = paymentRequest.getPayment();
        paymentId.setEndToEndId(payment.getUniqueId());
        paymentId.setInstructionId(UUID.randomUUID().toString());
      String executionDate =
                Optional.ofNullable(payment.getExecutionDate())
                        .map(
                                localDate ->
                                        localDate.atStartOfDay(ZoneId.of("CET") ).format( DateTimeFormatter.ISO_DATE_TIME ))
                        .orElse(ZonedDateTime.now( ZoneId.of("CET") ).format( DateTimeFormatter.ISO_DATE_TIME ));

        String unstructuredRemittance =
                Optional.ofNullable(payment.getReference()).map(Reference::getValue).orElse("");
        remittanceInformation.add(unstructuredRemittance);
        RemittanceInformationEntity remittanceInformationEntity = new RemittanceInformationEntity();
        remittanceInformationEntity.setUnstructured(remittanceInformation);
        transactions.add(
                new CreditTransferTransactionEntity.Builder()
                        .withPaymentId(paymentId)
                        .withInstructedAmount(instructedAmount)
                        .withRequestedExecutionDate(executionDate)
                        .withRemittanceInformation(remittanceInformationEntity )
                        .build());
        return transactions;
    }

    public CreditTransferTransactionEntity() {}

    private CreditTransferTransactionEntity(Builder builder) {
        this.paymentId = builder.paymentId;
        this.requestedExecutionDate = builder.requestedExecutionDate;
        this.instructedAmount = builder.instructedAmount;
        this.remittanceInformation = builder.remittanceInformation;
    }

    public String getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public InstructedAmountEntity getInstructedAmount() {
        return instructedAmount;
    }

    public static class Builder {
        private PaymentId paymentId;
        private String requestedExecutionDate;
        private InstructedAmountEntity instructedAmount;
        private RemittanceInformationEntity remittanceInformation;

        public Builder withPaymentId(PaymentId paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder withRequestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public Builder withInstructedAmount(InstructedAmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withRemittanceInformation(RemittanceInformationEntity remittanceInformation) {
            this.remittanceInformation = remittanceInformation;
            return this;
        }

        public CreditTransferTransactionEntity build() {
            return new CreditTransferTransactionEntity(this);
        }
    }
}
