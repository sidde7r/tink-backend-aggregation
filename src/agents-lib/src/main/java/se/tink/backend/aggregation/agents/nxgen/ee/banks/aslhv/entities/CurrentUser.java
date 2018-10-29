package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrentUser {
    int baseCurrencyId;
    int gender;
    String name;
    int type;
    int userId;
    String dateOfBirth;

    @JsonGetter("base_currency_id")
    public int getBaseCurrencyId() {
        return baseCurrencyId;
    }

    public int getGender() {
        return gender;
    }

    public int getType() {
        return type;
    }

    @JsonGetter("user_id")
    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    @JsonGetter("date_of_birth")
    public String getDateOfBirth() {
        return dateOfBirth;
    }
}