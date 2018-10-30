package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrentUser {
    int baseCurrencyId;
    String name;
    int userId;

    @JsonGetter("base_currency_id")
    public int getBaseCurrencyId() {
        return baseCurrencyId;
    }

    @JsonGetter("user_id")
    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}
