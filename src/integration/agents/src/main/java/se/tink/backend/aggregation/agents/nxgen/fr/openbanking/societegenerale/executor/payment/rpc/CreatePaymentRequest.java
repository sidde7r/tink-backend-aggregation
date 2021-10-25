package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PartyIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator.ValidatablePaymentRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest implements ValidatablePaymentRequest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private String paymentInformationId;
    private String creationDateTime;
    private int numberOfTransactions;
    private PartyIdentificationEntity initiatingParty;
    private PaymentTypeInformationEntity paymentTypeInformation;
    private DebtorAccountEntity debtorAccount;
    private BeneficiaryEntity beneficiary;
    private String chargeBearer;
    private String requestedExecutionDate;
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
        this.chargeBearer = builder.chargeBearer;
        this.requestedExecutionDate = builder.requestedExecutionDate;
        this.creditTransferTransaction = builder.creditTransferTransaction;
        this.supplementaryData = builder.supplementaryData;
    }

    @JsonIgnore
    @Override
    public String getLocalInstrument() {
        return paymentTypeInformation.getLocalInstrument();
    }

    @JsonIgnore
    @Override
    public LocalDate getRequestedExecutionDateAsLocalDate() {
        return LocalDate.parse(requestedExecutionDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static class Builder {
        private String paymentInformationId;
        private String creationDateTime;
        private int numberOfTransactions;
        private PartyIdentificationEntity initiatingParty;
        private PaymentTypeInformationEntity paymentTypeInformation;
        private DebtorAccountEntity debtorAccount;
        private String chargeBearer;
        private BeneficiaryEntity beneficiary;
        private String requestedExecutionDate;
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

        public Builder withChargeBearer(String chargeBearer) {
            this.chargeBearer = chargeBearer;
            return this;
        }

        public Builder withBeneficiary(BeneficiaryEntity beneficiary) {
            this.beneficiary = beneficiary;
            return this;
        }

        public Builder withRequestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
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
