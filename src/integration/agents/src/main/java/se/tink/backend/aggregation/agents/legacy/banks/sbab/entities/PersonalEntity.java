package se.tink.backend.aggregation.agents.banks.sbab.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PersonalEntity {
    PartEntity part;

    public String GetUserName() {
        return part.firstName + " " + part.lastName;
    }

    public String GetSSN() {
        return part.personalIdentityNumber;
    }
}
