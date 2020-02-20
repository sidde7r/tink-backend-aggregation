package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BPostBankAccountsResponseDTO {

    @JsonProperty("current-account")
    List<BPostBankAccountDTO> currentAccounts;
}
