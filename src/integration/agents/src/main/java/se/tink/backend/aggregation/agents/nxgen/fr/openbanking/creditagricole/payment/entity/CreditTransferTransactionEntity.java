package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreditTransferTransactionEntity {
    @JsonProperty("paymentId")
    private PaymentIdentificationEntity paymentId = null;

    @JsonProperty("requestedExecutionDate")
    private String requestedExecutionDate = null;

    @JsonProperty("endDate")
    private String endDate = null;

    @JsonProperty("executionRule")
    private ExecutionRuleEntity executionRule = null;

    @JsonProperty("frequency")
    private FrequencyCodeEntity frequency = null;

    @JsonProperty("instructedAmount")
    private AmountTypeEntity instructedAmount = null;

    @JsonProperty("beneficiary")
    private BeneficiaryEntity beneficiary = null;

    @JsonProperty("ultimateCreditor")
    private PartyIdentificationEntity ultimateCreditor = null;

    @JsonProperty("regulatoryReportingCodes")
    private RegulatoryReportingCodesEntity regulatoryReportingCodes = null;

    @JsonProperty("remittanceInformation")
    private RemittanceInformationEntity remittanceInformation = null;

    @JsonProperty("transactionStatus")
    private TransactionIndividualStatusCodeEntity transactionStatus = null;

    @JsonProperty("statusReasonInformation")
    private StatusReasonInformationEntity statusReasonInformation = null;

    public CreditTransferTransactionEntity() {}

    private CreditTransferTransactionEntity(
            PaymentIdentificationEntity paymentId,
            String requestedExecutionDate,
            String endDate,
            ExecutionRuleEntity executionRule,
            FrequencyCodeEntity frequency,
            AmountTypeEntity instructedAmount,
            BeneficiaryEntity beneficiary,
            PartyIdentificationEntity ultimateCreditor,
            RegulatoryReportingCodesEntity regulatoryReportingCodes,
            RemittanceInformationEntity remittanceInformation,
            TransactionIndividualStatusCodeEntity transactionStatus,
            StatusReasonInformationEntity statusReasonInformation) {
        this.paymentId = paymentId;
        this.requestedExecutionDate = requestedExecutionDate;
        this.endDate = endDate;
        this.executionRule = executionRule;
        this.frequency = frequency;
        this.instructedAmount = instructedAmount;
        this.beneficiary = beneficiary;
        this.ultimateCreditor = ultimateCreditor;
        this.regulatoryReportingCodes = regulatoryReportingCodes;
        this.remittanceInformation = remittanceInformation;
        this.transactionStatus = transactionStatus;
        this.statusReasonInformation = statusReasonInformation;
    }

    public static CreditTransferTransactionEntityBuilder builder() {
        return new CreditTransferTransactionEntityBuilder();
    }

    public PaymentIdentificationEntity getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(PaymentIdentificationEntity paymentId) {
        this.paymentId = paymentId;
    }

    public String getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(String requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public ExecutionRuleEntity getExecutionRule() {
        return executionRule;
    }

    public void setExecutionRule(ExecutionRuleEntity executionRule) {
        this.executionRule = executionRule;
    }

    public FrequencyCodeEntity getFrequency() {
        return frequency;
    }

    public void setFrequency(FrequencyCodeEntity frequency) {
        this.frequency = frequency;
    }

    public AmountTypeEntity getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(AmountTypeEntity instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public BeneficiaryEntity getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(BeneficiaryEntity beneficiary) {
        this.beneficiary = beneficiary;
    }

    public PartyIdentificationEntity getUltimateCreditor() {
        return ultimateCreditor;
    }

    public void setUltimateCreditor(PartyIdentificationEntity ultimateCreditor) {
        this.ultimateCreditor = ultimateCreditor;
    }

    public RegulatoryReportingCodesEntity getRegulatoryReportingCodes() {
        return regulatoryReportingCodes;
    }

    public void setRegulatoryReportingCodes(
            RegulatoryReportingCodesEntity regulatoryReportingCodes) {
        this.regulatoryReportingCodes = regulatoryReportingCodes;
    }

    public RemittanceInformationEntity getRemittanceInformation() {
        return remittanceInformation;
    }

    public void setRemittanceInformation(RemittanceInformationEntity remittanceInformation) {
        this.remittanceInformation = remittanceInformation;
    }

    public TransactionIndividualStatusCodeEntity getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionIndividualStatusCodeEntity transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public StatusReasonInformationEntity getStatusReasonInformation() {
        return statusReasonInformation;
    }

    public void setStatusReasonInformation(StatusReasonInformationEntity statusReasonInformation) {
        this.statusReasonInformation = statusReasonInformation;
    }

    public static class CreditTransferTransactionEntityBuilder {

        private PaymentIdentificationEntity paymentId;
        private String requestedExecutionDate;
        private String endDate;
        private ExecutionRuleEntity executionRule;
        private FrequencyCodeEntity frequency;
        private AmountTypeEntity instructedAmount;
        private BeneficiaryEntity beneficiary;
        private PartyIdentificationEntity ultimateCreditor;
        private RegulatoryReportingCodesEntity regulatoryReportingCodes;
        private RemittanceInformationEntity remittanceInformation;
        private TransactionIndividualStatusCodeEntity transactionStatus;
        private StatusReasonInformationEntity statusReasonInformation;

        CreditTransferTransactionEntityBuilder() {}

        public CreditTransferTransactionEntityBuilder paymentId(
                PaymentIdentificationEntity paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public CreditTransferTransactionEntityBuilder requestedExecutionDate(
                String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public CreditTransferTransactionEntityBuilder endDate(String endDate) {
            this.endDate = endDate;
            return this;
        }

        public CreditTransferTransactionEntityBuilder executionRule(
                ExecutionRuleEntity executionRule) {
            this.executionRule = executionRule;
            return this;
        }

        public CreditTransferTransactionEntityBuilder frequency(FrequencyCodeEntity frequency) {
            this.frequency = frequency;
            return this;
        }

        public CreditTransferTransactionEntityBuilder instructedAmount(
                AmountTypeEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public CreditTransferTransactionEntityBuilder beneficiary(BeneficiaryEntity beneficiary) {
            this.beneficiary = beneficiary;
            return this;
        }

        public CreditTransferTransactionEntityBuilder ultimateCreditor(
                PartyIdentificationEntity ultimateCreditor) {
            this.ultimateCreditor = ultimateCreditor;
            return this;
        }

        public CreditTransferTransactionEntityBuilder regulatoryReportingCodes(
                RegulatoryReportingCodesEntity regulatoryReportingCodes) {
            this.regulatoryReportingCodes = regulatoryReportingCodes;
            return this;
        }

        public CreditTransferTransactionEntityBuilder remittanceInformation(
                RemittanceInformationEntity remittanceInformation) {
            this.remittanceInformation = remittanceInformation;
            return this;
        }

        public CreditTransferTransactionEntityBuilder transactionStatus(
                TransactionIndividualStatusCodeEntity transactionStatus) {
            this.transactionStatus = transactionStatus;
            return this;
        }

        public CreditTransferTransactionEntityBuilder statusReasonInformation(
                StatusReasonInformationEntity statusReasonInformation) {
            this.statusReasonInformation = statusReasonInformation;
            return this;
        }

        public CreditTransferTransactionEntity build() {
            return new CreditTransferTransactionEntity(
                    paymentId,
                    requestedExecutionDate,
                    endDate,
                    executionRule,
                    frequency,
                    instructedAmount,
                    beneficiary,
                    ultimateCreditor,
                    regulatoryReportingCodes,
                    remittanceInformation,
                    transactionStatus,
                    statusReasonInformation);
        }

        public String toString() {
            return "CreditTransferTransactionEntity.CreditTransferTransactionEntityBuilder(paymentId="
                    + this.paymentId
                    + ", requestedExecutionDate="
                    + this.requestedExecutionDate
                    + ", endDate="
                    + this.endDate
                    + ", executionRule="
                    + this.executionRule
                    + ", frequency="
                    + this.frequency
                    + ", instructedAmount="
                    + this.instructedAmount
                    + ", beneficiary="
                    + this.beneficiary
                    + ", ultimateCreditor="
                    + this.ultimateCreditor
                    + ", regulatoryReportingCodes="
                    + this.regulatoryReportingCodes
                    + ", remittanceInformation="
                    + this.remittanceInformation
                    + ", transactionStatus="
                    + this.transactionStatus
                    + ", statusReasonInformation="
                    + this.statusReasonInformation
                    + ")";
        }
    }
}
