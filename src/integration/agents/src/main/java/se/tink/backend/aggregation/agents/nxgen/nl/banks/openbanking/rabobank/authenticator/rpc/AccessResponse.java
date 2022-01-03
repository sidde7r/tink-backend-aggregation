package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.AccountConsent;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@ToString
public class AccessResponse {

    private final Map<String, List<AccountConsent>> scopes = new HashMap<>();

    @JsonAnySetter
    public void setScopes(String name, List<AccountConsent> accountConsents) {
        scopes.put(name, accountConsents);
    }
}
