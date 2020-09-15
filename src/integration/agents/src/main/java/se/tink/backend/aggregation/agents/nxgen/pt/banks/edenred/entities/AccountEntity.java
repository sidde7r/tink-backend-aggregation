package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class AccountEntity {

    private String iban;
    private String cardNumber;
    private double availableBalance;
    private String cardHolderFirstName;
    private String cardHolderLastName;
    private boolean cardActivated;
}
