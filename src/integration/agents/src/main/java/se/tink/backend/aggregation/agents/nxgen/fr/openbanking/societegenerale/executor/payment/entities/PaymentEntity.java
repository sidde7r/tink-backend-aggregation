package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class PaymentEntity {
    private String resourceId;
    private String paymentInformationId;
    private String creationDateTime;
    private int numberOfTransactions;
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
