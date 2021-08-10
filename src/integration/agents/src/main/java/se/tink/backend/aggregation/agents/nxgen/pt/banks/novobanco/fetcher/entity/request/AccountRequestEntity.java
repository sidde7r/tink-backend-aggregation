package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountRequestEntity {
    public AccountRequestEntity(String accountId) {
        this.accountId = accountId;
    }

    @JsonProperty("Conta")
    private String accountId;
}
