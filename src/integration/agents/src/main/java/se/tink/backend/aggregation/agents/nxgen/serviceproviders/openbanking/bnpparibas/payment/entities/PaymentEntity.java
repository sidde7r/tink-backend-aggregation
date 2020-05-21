package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentEntity {
    private String paymentInformationStatus;
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
