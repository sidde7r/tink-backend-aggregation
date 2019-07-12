package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditTransferTransactionEntity {
    @JsonProperty("paymentId")
    private PaymentIdentificationEntity paymentId = null;

    @JsonProperty("requestedExecutionDate")
    private OffsetDateTime requestedExecutionDate = null;

    @JsonProperty("endDate")
    private OffsetDateTime endDate = null;

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

    public PaymentIdentificationEntity getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(PaymentIdentificationEntity paymentId) {
        this.paymentId = paymentId;
    }

    public OffsetDateTime getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(OffsetDateTime requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
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
}
