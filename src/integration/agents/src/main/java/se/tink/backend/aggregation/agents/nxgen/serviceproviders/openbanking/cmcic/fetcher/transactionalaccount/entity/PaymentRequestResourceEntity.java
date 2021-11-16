package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@AllArgsConstructor
@Jacksonized
@Builder
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

    @JsonProperty("chargeBearer")
    private ChargeBearerCodeEntity chargeBearer;

    @JsonProperty("paymentInformationStatus")
    private PaymentInformationStatusEntity paymentInformationStatus;

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
}
