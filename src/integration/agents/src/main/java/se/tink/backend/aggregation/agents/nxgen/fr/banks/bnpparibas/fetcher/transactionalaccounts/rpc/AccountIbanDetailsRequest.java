package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIbanDetailsRequest {
    @JsonProperty("modeBeneficiaire")
    private int modeBeneficiaire;

    public AccountIbanDetailsRequest(int modeBeneficiaire) {
        this.modeBeneficiaire = modeBeneficiaire;
    }
}
