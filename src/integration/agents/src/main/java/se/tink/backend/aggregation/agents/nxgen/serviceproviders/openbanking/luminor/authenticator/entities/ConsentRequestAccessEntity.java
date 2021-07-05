package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequestAccessEntity {
    @JsonProperty("accounts")
    List<AccountAccessEntity> accounts;

    public ConsentRequestAccessEntity(List<String> ibans) {
        List<AccountAccessEntity> bodyAccounts = new ArrayList<>();
        for (String iban : ibans) {
            AccountAccessEntity populateAccounts = new AccountAccessEntity();
            populateAccounts.setIban(iban);
            bodyAccounts.add(populateAccounts);
        }
        this.accounts = bodyAccounts;
    }
}
