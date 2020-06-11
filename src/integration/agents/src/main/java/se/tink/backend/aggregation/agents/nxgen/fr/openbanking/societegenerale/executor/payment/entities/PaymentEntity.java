package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import java.util.List;
import lombok.*;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class PaymentEntity {
    private String resourceId;
    private BeneficiaryEntity beneficiary;
    private PaymentInformationStatusCodeEntity paymentInformationStatus;
    private StatusReasonInformationEntity statusReasonInformation;
    private List<CreditTransferTransactionEntity> creditTransferTransaction;
    private SupplementaryDataEntity supplementaryData;

    public InstructedAmountEntity getAmountFromResponse() {
        return creditTransferTransaction.stream()
                .findFirst()
                .map(CreditTransferTransactionEntity::getInstructedAmount)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Amount cannot be extracted from the response"));
    }
}
