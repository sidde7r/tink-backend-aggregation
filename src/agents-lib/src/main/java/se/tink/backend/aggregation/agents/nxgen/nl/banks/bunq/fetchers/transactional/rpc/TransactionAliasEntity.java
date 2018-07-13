package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAliasEntity {
    private String iban;
    @JsonProperty("is_light")
    private boolean light;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("label_user")
    private LabelUserEntity labelUser;
    private String country;

    public String getIban() {
        return iban;
    }

    public boolean isLight() {
        return light;
    }

    public String getDisplayName() {
        return displayName;
    }

    public LabelUserEntity getLabelUser() {
        return labelUser;
    }

    public String getCountry() {
        return country;
    }
}
