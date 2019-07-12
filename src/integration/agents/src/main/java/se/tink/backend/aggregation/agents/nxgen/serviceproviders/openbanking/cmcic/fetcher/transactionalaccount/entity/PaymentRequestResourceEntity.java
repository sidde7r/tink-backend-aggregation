package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentRequestResourceEntity {
    @JsonProperty("resourceId")
    private String resourceId = null;

    @JsonProperty("paymentInformationId")
    private String paymentInformationId = null;

    @JsonProperty("creationDateTime")
    private OffsetDateTime creationDateTime = null;

    @JsonProperty("numberOfTransactions")
    private Integer numberOfTransactions = null;

    @JsonProperty("initiatingParty")
    private PartyIdentificationEntity initiatingParty = null;

    @JsonProperty("paymentTypeInformation")
    private PaymentTypeInformationEntity paymentTypeInformation = null;

    @JsonProperty("debtor")
    private PartyIdentificationEntity debtor = null;

    @JsonProperty("debtorAccount")
    private AccountIdentificationEntity debtorAccount = null;

    @JsonProperty("debtorAgent")
    private FinancialInstitutionIdentificationEntity debtorAgent = null;

    @JsonProperty("beneficiary")
    private BeneficiaryEntity beneficiary = null;

    @JsonProperty("ultimateCreditor")
    private PartyIdentificationEntity ultimateCreditor = null;

    @JsonProperty("purpose")
    private PurposeCodeEntity purpose = null;

    @JsonProperty("chargeBearer")
    private ChargeBearerCodeEntity chargeBearer = null;

    @JsonProperty("paymentInformationStatus")
    private PaymentInformationStatusCodeEntity paymentInformationStatus = null;

    @JsonProperty("statusReasonInformation")
    private StatusReasonInformationEntity statusReasonInformation = null;

    @JsonProperty("fundsAvailability")
    private Boolean fundsAvailability = null;

    @JsonProperty("booking")
    private Boolean booking = null;

    @JsonProperty("requestedExecutionDate")
    private OffsetDateTime requestedExecutionDate = null;

    @JsonProperty("creditTransferTransaction")
    private List<CreditTransferTransactionEntity> creditTransferTransaction =
            new ArrayList<CreditTransferTransactionEntity>();

    @JsonProperty("supplementaryData")
    private SupplementaryDataEntity supplementaryData = null;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getPaymentInformationId() {
        return paymentInformationId;
    }

    public void setPaymentInformationId(String paymentInformationId) {
        this.paymentInformationId = paymentInformationId;
    }

    public OffsetDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(OffsetDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Integer getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(Integer numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public PartyIdentificationEntity getInitiatingParty() {
        return initiatingParty;
    }

    public void setInitiatingParty(PartyIdentificationEntity initiatingParty) {
        this.initiatingParty = initiatingParty;
    }

    public PaymentTypeInformationEntity getPaymentTypeInformation() {
        return paymentTypeInformation;
    }

    public void setPaymentTypeInformation(PaymentTypeInformationEntity paymentTypeInformation) {
        this.paymentTypeInformation = paymentTypeInformation;
    }

    public PartyIdentificationEntity getDebtor() {
        return debtor;
    }

    public void setDebtor(PartyIdentificationEntity debtor) {
        this.debtor = debtor;
    }

    public AccountIdentificationEntity getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(AccountIdentificationEntity debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public FinancialInstitutionIdentificationEntity getDebtorAgent() {
        return debtorAgent;
    }

    public void setDebtorAgent(FinancialInstitutionIdentificationEntity debtorAgent) {
        this.debtorAgent = debtorAgent;
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

    public PurposeCodeEntity getPurpose() {
        return purpose;
    }

    public void setPurpose(PurposeCodeEntity purpose) {
        this.purpose = purpose;
    }

    public ChargeBearerCodeEntity getChargeBearer() {
        return chargeBearer;
    }

    public void setChargeBearer(ChargeBearerCodeEntity chargeBearer) {
        this.chargeBearer = chargeBearer;
    }

    public PaymentInformationStatusCodeEntity getPaymentInformationStatus() {
        return paymentInformationStatus;
    }

    public void setPaymentInformationStatus(
            PaymentInformationStatusCodeEntity paymentInformationStatus) {
        this.paymentInformationStatus = paymentInformationStatus;
    }

    public StatusReasonInformationEntity getStatusReasonInformation() {
        return statusReasonInformation;
    }

    public void setStatusReasonInformation(StatusReasonInformationEntity statusReasonInformation) {
        this.statusReasonInformation = statusReasonInformation;
    }

    public Boolean getFundsAvailability() {
        return fundsAvailability;
    }

    public void setFundsAvailability(Boolean fundsAvailability) {
        this.fundsAvailability = fundsAvailability;
    }

    public Boolean getBooking() {
        return booking;
    }

    public void setBooking(Boolean booking) {
        this.booking = booking;
    }

    public OffsetDateTime getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(OffsetDateTime requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public List<CreditTransferTransactionEntity> getCreditTransferTransaction() {
        return creditTransferTransaction;
    }

    public void setCreditTransferTransaction(
            List<CreditTransferTransactionEntity> creditTransferTransaction) {
        this.creditTransferTransaction = creditTransferTransaction;
    }

    public SupplementaryDataEntity getSupplementaryData() {
        return supplementaryData;
    }

    public void setSupplementaryData(SupplementaryDataEntity supplementaryData) {
        this.supplementaryData = supplementaryData;
    }
}
