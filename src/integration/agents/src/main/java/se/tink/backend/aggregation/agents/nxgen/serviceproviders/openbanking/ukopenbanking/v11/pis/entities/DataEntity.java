package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class DataEntity {
    // Populated in response
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("PaymentId")
    private String paymentId;

    // Populated in response
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("Status")
    private UkOpenBankingApiDefinitions.TransactionIndividualStatus1Code status;

    // Populated in response
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("CreationDateTime")
    private String creationDateTime;

    @JsonProperty("Initiation")
    private InitiationEntity initiation;

    private DataEntity(
            @JsonProperty("PaymentId") String paymentId,
            @JsonProperty("Status")
                    UkOpenBankingApiDefinitions.TransactionIndividualStatus1Code status,
            @JsonProperty("CreationDateTime") String creationDateTime,
            @JsonProperty("Initiation") InitiationEntity initiation) {
        this.paymentId = paymentId;
        this.status = status;
        this.creationDateTime = creationDateTime;
        this.initiation = initiation;
    }

    @JsonIgnore
    private DataEntity(InitiationEntity initiation) {
        this.initiation = initiation;
    }

    @JsonIgnore
    private DataEntity(String paymentId, InitiationEntity initiation) {
        this.initiation = initiation;
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public UkOpenBankingConstants.TransactionIndividualStatus1Code getStatus() {
        return status;
    }

    @JsonIgnore
    public static DataEntity createPersonToPerson(
            String internalTransferId,
            String externalTransferId,
            DebtorCreditorAccountEntity sourceAccount,
            DebtorCreditorAccountEntity destinationAccount,
            Amount amount,
            String bankTransferMessage) {

        return new DataEntity(
                InitiationEntity.createPersonToPerson(
                        internalTransferId,
                        externalTransferId,
                        sourceAccount,
                        destinationAccount,
                        amount,
                        bankTransferMessage));
    }

    @JsonIgnore
    public static DataEntity createPersonToPerson(
            String paymentId,
            String internalTransferId,
            String externalTransferId,
            DebtorCreditorAccountEntity sourceAccount,
            DebtorCreditorAccountEntity destinationAccount,
            Amount amount,
            String bankTransferMessage) {

        return new DataEntity(
                paymentId,
                InitiationEntity.createPersonToPerson(
                        internalTransferId,
                        externalTransferId,
                        sourceAccount,
                        destinationAccount,
                        amount,
                        bankTransferMessage));
    }
}
