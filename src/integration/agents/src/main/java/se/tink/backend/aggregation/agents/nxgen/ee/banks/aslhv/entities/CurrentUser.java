package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrentUser {
    @JsonProperty("base_currency_id")
    int baseCurrencyId;

    @JsonProperty("name")
    String name;

    @JsonProperty("user_id")
    int userId;

    @JsonIgnore
    public int getBaseCurrencyId() {
        return baseCurrencyId;
    }

    @JsonIgnore
    public int getUserId() {
        return userId;
    }

    @JsonIgnore
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
}
