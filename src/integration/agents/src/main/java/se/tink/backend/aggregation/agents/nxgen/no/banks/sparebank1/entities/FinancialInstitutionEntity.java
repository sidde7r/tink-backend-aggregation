package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialInstitutionEntity {
    private String id;
    private String name;
    private String type;
    private PhoneNumberEntity phoneNumber;
    private String email;
    private String openingHours;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public PhoneNumberEntity getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }
}
