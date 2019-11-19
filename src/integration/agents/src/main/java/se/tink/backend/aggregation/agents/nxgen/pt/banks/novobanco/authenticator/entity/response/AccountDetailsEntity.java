package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDetailsEntity {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Iban")
    private String iban;

    @JsonProperty("Desc")
    private String desc;

    public String getId() {
        return id;
    }

    public String getIban() {
        return iban;
    }

    public String getDesc() {
        return desc;
    }
}
