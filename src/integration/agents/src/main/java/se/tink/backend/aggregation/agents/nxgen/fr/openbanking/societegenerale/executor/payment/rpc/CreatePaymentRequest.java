package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PartyIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;


@JsonObject
public class CreatePaymentRequest {
    private String paymentInformationId;
    private String creationDateTime;
    private int numberOfTransactions;
    private PartyIdentificationEntity initiatingParty;
    private PaymentTypeInformationEntity paymentTypeInformation;
    private DebtorAccountEntity debtorAccount;
    private BeneficiaryEntity beneficiary;
    private List<CreditTransferTransactionEntity> creditTransferTransaction;
    private SupplementaryDataEntity supplementaryData;

    @JsonIgnore
    private CreatePaymentRequest(Builder builder) {
        this.paymentInformationId = builder.paymentInformationId;
        this.creationDateTime = builder.creationDateTime;
        this.numberOfTransactions = builder.numberOfTransactions;
        this.initiatingParty = builder.initiatingParty;
        this.paymentTypeInformation = builder.paymentTypeInformation;
        this.debtorAccount = builder.debtorAccount;
        this.beneficiary = builder.beneficiary;
        this.creditTransferTransaction = builder.creditTransferTransaction;
        this.supplementaryData = builder.supplementaryData;
        }

    public static class Builder {
        private String paymentInformationId;
        private String creationDateTime;
        private int numberOfTransactions;
        private PartyIdentificationEntity initiatingParty;
        private PaymentTypeInformationEntity paymentTypeInformation;
        private PartyIdentificationEntity debtor;
        private DebtorAccountEntity debtorAccount;
        private BeneficiaryEntity beneficiary;
        private List<CreditTransferTransactionEntity> creditTransferTransaction;
        private SupplementaryDataEntity supplementaryData;

        public Builder withPaymentInformationId(String paymentInformationId) {
            this.paymentInformationId = paymentInformationId;
            return this;
        }

        public Builder withCreationDateTime(String creationDateTime) {
            this.creationDateTime = creationDateTime;
            return this;
        }

        public Builder withNumberOfTransactions(int numberOfTransactions) {
            this.numberOfTransactions = numberOfTransactions;
            return this;
        }

        public Builder withInitiatingParty(PartyIdentificationEntity initiatingParty) {
            this.initiatingParty = initiatingParty;
            return this;
        }

        public Builder withPaymentTypeInformation(
                PaymentTypeInformationEntity paymentTypeInformation) {
            this.paymentTypeInformation = paymentTypeInformation;
            return this;
        }

        public Builder withDebtorAccount(DebtorAccountEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withBeneficiary(BeneficiaryEntity beneficiary) {
            this.beneficiary = beneficiary;
            return this;
        }


        public Builder withCreditTransferTransaction(
                List<CreditTransferTransactionEntity> creditTransferTransaction) {
            this.creditTransferTransaction = creditTransferTransaction;
            return this;
        }

        public Builder withSupplementaryData(SupplementaryDataEntity supplementaryData) {
            this.supplementaryData = supplementaryData;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }

    }

