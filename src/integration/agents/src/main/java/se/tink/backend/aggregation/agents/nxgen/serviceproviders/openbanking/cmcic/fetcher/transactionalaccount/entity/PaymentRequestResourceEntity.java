package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class PaymentRequestResourceEntity {
    @JsonProperty("resourceId")
    private String resourceId = null;

    @JsonProperty("paymentInformationId")
    private String paymentInformationId = null;

    @JsonProperty("creationDateTime")
    private String creationDateTime = null;

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
    private String requestedExecutionDate = null;

    @JsonProperty("creditTransferTransaction")
    private List<CreditTransferTransactionEntity> creditTransferTransaction =
            new ArrayList<CreditTransferTransactionEntity>();

    @JsonProperty("supplementaryData")
    private SupplementaryDataEntity supplementaryData = null;

    public PaymentRequestResourceEntity() {}

    private PaymentRequestResourceEntity(
            String resourceId,
            String paymentInformationId,
            String creationDateTime,
            Integer numberOfTransactions,
            PartyIdentificationEntity initiatingParty,
            PaymentTypeInformationEntity paymentTypeInformation,
            PartyIdentificationEntity debtor,
            AccountIdentificationEntity debtorAccount,
            FinancialInstitutionIdentificationEntity debtorAgent,
            BeneficiaryEntity beneficiary,
            PartyIdentificationEntity ultimateCreditor,
            PurposeCodeEntity purpose,
            ChargeBearerCodeEntity chargeBearer,
            PaymentInformationStatusCodeEntity paymentInformationStatus,
            StatusReasonInformationEntity statusReasonInformation,
            Boolean fundsAvailability,
            Boolean booking,
            String requestedExecutionDate,
            List<CreditTransferTransactionEntity> creditTransferTransaction,
            SupplementaryDataEntity supplementaryData) {
        this.resourceId = resourceId;
        this.paymentInformationId = paymentInformationId;
        this.creationDateTime = creationDateTime;
        this.numberOfTransactions = numberOfTransactions;
        this.initiatingParty = initiatingParty;
        this.paymentTypeInformation = paymentTypeInformation;
        this.debtor = debtor;
        this.debtorAccount = debtorAccount;
        this.debtorAgent = debtorAgent;
        this.beneficiary = beneficiary;
        this.ultimateCreditor = ultimateCreditor;
        this.purpose = purpose;
        this.chargeBearer = chargeBearer;
        this.paymentInformationStatus = paymentInformationStatus;
        this.statusReasonInformation = statusReasonInformation;
        this.fundsAvailability = fundsAvailability;
        this.booking = booking;
        this.requestedExecutionDate = requestedExecutionDate;
        this.creditTransferTransaction = creditTransferTransaction;
        this.supplementaryData = supplementaryData;
    }

    public static PaymentRequestResourceEntityBuilder builder() {
        return new PaymentRequestResourceEntityBuilder();
    }

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

    public String getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(String creationDateTime) {
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

    public String getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(String requestedExecutionDate) {
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

    public static class PaymentRequestResourceEntityBuilder {

        private String resourceId;
        private String paymentInformationId;
        private String creationDateTime;
        private Integer numberOfTransactions;
        private PartyIdentificationEntity initiatingParty;
        private PaymentTypeInformationEntity paymentTypeInformation;
        private PartyIdentificationEntity debtor;
        private AccountIdentificationEntity debtorAccount;
        private FinancialInstitutionIdentificationEntity debtorAgent;
        private BeneficiaryEntity beneficiary;
        private PartyIdentificationEntity ultimateCreditor;
        private PurposeCodeEntity purpose;
        private ChargeBearerCodeEntity chargeBearer;
        private PaymentInformationStatusCodeEntity paymentInformationStatus;
        private StatusReasonInformationEntity statusReasonInformation;
        private Boolean fundsAvailability;
        private Boolean booking;
        private String requestedExecutionDate;
        private List<CreditTransferTransactionEntity> creditTransferTransaction;
        private SupplementaryDataEntity supplementaryData;

        PaymentRequestResourceEntityBuilder() {}

        public PaymentRequestResourceEntityBuilder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public PaymentRequestResourceEntityBuilder paymentInformationId(
                String paymentInformationId) {
            this.paymentInformationId = paymentInformationId;
            return this;
        }

        public PaymentRequestResourceEntityBuilder creationDateTime(String creationDateTime) {
            this.creationDateTime = creationDateTime;
            return this;
        }

        public PaymentRequestResourceEntityBuilder numberOfTransactions(
                Integer numberOfTransactions) {
            this.numberOfTransactions = numberOfTransactions;
            return this;
        }

        public PaymentRequestResourceEntityBuilder initiatingParty(
                PartyIdentificationEntity initiatingParty) {
            this.initiatingParty = initiatingParty;
            return this;
        }

        public PaymentRequestResourceEntityBuilder paymentTypeInformation(
                PaymentTypeInformationEntity paymentTypeInformation) {
            this.paymentTypeInformation = paymentTypeInformation;
            return this;
        }

        public PaymentRequestResourceEntityBuilder debtor(PartyIdentificationEntity debtor) {
            this.debtor = debtor;
            return this;
        }

        public PaymentRequestResourceEntityBuilder debtorAccount(
                AccountIdentificationEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public PaymentRequestResourceEntityBuilder debtorAgent(
                FinancialInstitutionIdentificationEntity debtorAgent) {
            this.debtorAgent = debtorAgent;
            return this;
        }

        public PaymentRequestResourceEntityBuilder beneficiary(BeneficiaryEntity beneficiary) {
            this.beneficiary = beneficiary;
            return this;
        }

        public PaymentRequestResourceEntityBuilder ultimateCreditor(
                PartyIdentificationEntity ultimateCreditor) {
            this.ultimateCreditor = ultimateCreditor;
            return this;
        }

        public PaymentRequestResourceEntityBuilder purpose(PurposeCodeEntity purpose) {
            this.purpose = purpose;
            return this;
        }

        public PaymentRequestResourceEntityBuilder chargeBearer(
                ChargeBearerCodeEntity chargeBearer) {
            this.chargeBearer = chargeBearer;
            return this;
        }

        public PaymentRequestResourceEntityBuilder paymentInformationStatus(
                PaymentInformationStatusCodeEntity paymentInformationStatus) {
            this.paymentInformationStatus = paymentInformationStatus;
            return this;
        }

        public PaymentRequestResourceEntityBuilder statusReasonInformation(
                StatusReasonInformationEntity statusReasonInformation) {
            this.statusReasonInformation = statusReasonInformation;
            return this;
        }

        public PaymentRequestResourceEntityBuilder fundsAvailability(Boolean fundsAvailability) {
            this.fundsAvailability = fundsAvailability;
            return this;
        }

        public PaymentRequestResourceEntityBuilder booking(Boolean booking) {
            this.booking = booking;
            return this;
        }

        public PaymentRequestResourceEntityBuilder requestedExecutionDate(
                String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public PaymentRequestResourceEntityBuilder creditTransferTransaction(
                List<CreditTransferTransactionEntity> creditTransferTransaction) {
            this.creditTransferTransaction = creditTransferTransaction;
            return this;
        }

        public PaymentRequestResourceEntityBuilder supplementaryData(
                SupplementaryDataEntity supplementaryData) {
            this.supplementaryData = supplementaryData;
            return this;
        }

        public PaymentRequestResourceEntity build() {
            return new PaymentRequestResourceEntity(
                    resourceId,
                    paymentInformationId,
                    creationDateTime,
                    numberOfTransactions,
                    initiatingParty,
                    paymentTypeInformation,
                    debtor,
                    debtorAccount,
                    debtorAgent,
                    beneficiary,
                    ultimateCreditor,
                    purpose,
                    chargeBearer,
                    paymentInformationStatus,
                    statusReasonInformation,
                    fundsAvailability,
                    booking,
                    requestedExecutionDate,
                    creditTransferTransaction,
                    supplementaryData);
        }
    }
}
