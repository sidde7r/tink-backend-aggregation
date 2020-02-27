package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BPostBankAccountDTO {

    List<BPostBankAccountIdentifierDTO> accountIdentification;
    String currency;
    String availableBalance;
    String bookedBalance;

    @JsonProperty("clientShortNameHolder")
    String clientName;

    String alias;
}
