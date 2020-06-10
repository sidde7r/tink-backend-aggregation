package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreditTransferTransactionEntity {

    private PaymentId paymentId;
    private InstructedAmountEntity instructedAmount;
    private RemittanceInformationEntity remittanceInformation;

    @JsonIgnore
    public static List<CreditTransferTransactionEntity> of(PaymentRequest paymentRequest) {
        InstructedAmountEntity instructedAmount = InstructedAmountEntity.of(paymentRequest);
        List<CreditTransferTransactionEntity> transactions = new ArrayList<>();
        List<String> remittanceInformation = new ArrayList<>();

        Payment payment = paymentRequest.getPayment();

        PaymentId paymentId = new PaymentId(payment.getUniqueId(), UUID.randomUUID().toString());

        String unstructuredRemittance =
                Optional.ofNullable(payment.getReference()).map(Reference::getValue).orElse("");
        remittanceInformation.add(unstructuredRemittance);
        RemittanceInformationEntity remittanceInformationEntity = new RemittanceInformationEntity();
        remittanceInformationEntity.setUnstructured(remittanceInformation);
        transactions.add(
                CreditTransferTransactionEntity.builder()
                        .paymentId(paymentId)
                        .instructedAmount(instructedAmount)
                        .remittanceInformation(remittanceInformationEntity)
                        .build());
        return transactions;
    }
}
