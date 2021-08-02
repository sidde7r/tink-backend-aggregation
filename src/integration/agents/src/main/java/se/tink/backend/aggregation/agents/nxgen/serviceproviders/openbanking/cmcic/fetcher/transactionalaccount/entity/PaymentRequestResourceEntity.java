package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonInclude(Include.NON_NULL)
public class PaymentRequestResourceEntity {
    @JsonProperty("resourceId")
    private String resourceId;

    @JsonProperty("paymentInformationId")
    private String paymentInformationId;

    @JsonProperty("creationDateTime")
    private String creationDateTime;

    @JsonProperty("numberOfTransactions")
    private Integer numberOfTransactions;

    @JsonProperty("initiatingParty")
    private PartyIdentificationEntity initiatingParty;

    @JsonProperty("paymentTypeInformation")
    private PaymentTypeInformationEntity paymentTypeInformation;

    @JsonProperty("debtor")
    private PartyIdentificationEntity debtor;

    @JsonProperty("debtorAccount")
    private AccountIdentificationEntity debtorAccount;

    @JsonProperty("debtorAgent")
    private FinancialInstitutionIdentificationEntity debtorAgent;

    @JsonProperty("beneficiary")
    private BeneficiaryEntity beneficiary;

    @JsonProperty("ultimateCreditor")
    private PartyIdentificationEntity ultimateCreditor;

    @JsonProperty("purpose")
    private PurposeCodeEntity purpose;

    @JsonProperty("chargeBearer")
    private ChargeBearerCodeEntity chargeBearer;

    @JsonProperty("paymentInformationStatus")
    private PaymentInformationStatusCodeEntity paymentInformationStatus;

    @JsonProperty("statusReasonInformation")
    private StatusReasonInformationEntity statusReasonInformation;

    @JsonProperty("fundsAvailability")
    private Boolean fundsAvailability;

    @JsonProperty("booking")
    private Boolean booking;

    @JsonProperty("requestedExecutionDate")
    private String requestedExecutionDate;

    @JsonProperty("creditTransferTransaction")
    private List<CreditTransferTransactionEntity> creditTransferTransaction;

    @JsonProperty("supplementaryData")
    private SupplementaryDataEntity supplementaryData;

    @JsonCreator
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

    @JsonIgnore
    public static PaymentRequestResourceEntityBuilder builder() {
        return new PaymentRequestResourceEntityBuilder();
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
