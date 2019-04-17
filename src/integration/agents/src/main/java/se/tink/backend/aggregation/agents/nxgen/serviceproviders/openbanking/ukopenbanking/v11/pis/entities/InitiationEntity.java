package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class InitiationEntity {
    @JsonProperty("InstructionIdentification")
    private String instructionIdentification; // Max35Text

    @JsonProperty("EndToEndIdentification")
    private String endToEndIdentification; // Max35Text (UNIQUE ID)

    @JsonProperty("InstructedAmount")
    private InstructedAmountEntity instructedAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("DebtorAccount")
    private DebtorCreditorAccountEntity sourceAccount;

    @JsonProperty("CreditorAccount")
    private DebtorCreditorAccountEntity destinationAccount;

    @JsonProperty("RemittanceInformation")
    private RemittanceInformationEntity remittanceInformation;

    private InitiationEntity(
            @JsonProperty("InstructionIdentification") String instructionIdentification,
            @JsonProperty("EndToEndIdentification") String endToEndIdentification,
            @JsonProperty("InstructedAmount") InstructedAmountEntity instructedAmount,
            @JsonProperty("DebtorAccount") DebtorCreditorAccountEntity sourceAccount,
            @JsonProperty("CreditorAccount") DebtorCreditorAccountEntity destinationAccount,
            @JsonProperty("RemittanceInformation")
                    RemittanceInformationEntity remittanceInformation) {
        this.instructionIdentification = instructionIdentification;
        this.endToEndIdentification = endToEndIdentification;
        this.instructedAmount = instructedAmount;
        this.destinationAccount = destinationAccount;
        this.sourceAccount = sourceAccount;
        this.remittanceInformation = remittanceInformation;
    }

    @JsonIgnore
    public static InitiationEntity createPersonToPerson(
            String internalTransferId,
            String externalTransferId,
            DebtorCreditorAccountEntity sourceAccount,
            DebtorCreditorAccountEntity destinationAccount,
            Amount amount,
            String bankTransferMessage) {

        return new InitiationEntity(
                externalTransferId,
                internalTransferId,
                InstructedAmountEntity.create(amount),
                sourceAccount,
                destinationAccount,
                RemittanceInformationEntity.create(bankTransferMessage));
    }

    @JsonIgnore
    public String getExternalId() {
        return instructionIdentification;
    }
}
