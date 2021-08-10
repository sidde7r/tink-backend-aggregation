package se.tink.backend.aggregation.agents.banks.sbab.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PartEntity {
    String personalIdentityNumber;
    String firstName;
    String lastName;
    String taxCountryNumericCode;
    String citizenshipCountryNumericCode;
}
