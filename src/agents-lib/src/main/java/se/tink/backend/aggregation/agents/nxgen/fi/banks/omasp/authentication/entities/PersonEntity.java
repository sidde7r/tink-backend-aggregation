package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PersonEntity {
    private String welcome;
    private String firstName;
    private String lastName;

    public String getWelcome() {
        return welcome;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
