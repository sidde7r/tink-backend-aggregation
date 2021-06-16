package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {
    private String paymentInformationStatus;
    private String statusReasonInformation;
    private PaymentTypeInformationEntity paymentTypeInformation;
    private AccountEntity debtorAccount;
    private BeneficiaryEntity beneficiary;
    private List<CreditTransferTransactionEntity> creditTransferTransaction;

    public String getPaymentType() {
        return paymentTypeInformation.getServiceLevel();
    }

    public String getPaymentStatus() {
        return paymentInformationStatus;
    }

    public String getStatusReasonInformation() {
        return statusReasonInformation;
    }

    public AmountEntity getAmount() {
        return creditTransferTransaction.stream()
                .findFirst()
                .map(CreditTransferTransactionEntity::getAmount)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Amount cannot be extracted from the response"));
    }

    public AccountEntity getDebtorAccount() {
        return debtorAccount;
    }

    public AccountEntity getCreditorAccount() {
        return beneficiary.getCreditorAccount();
    }
}
