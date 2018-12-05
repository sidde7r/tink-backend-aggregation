package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserEntity {
    private String birthName;
    private String givenName;

    public String getBirthName() {
        return birthName;
    }

    public String getGivenName() {
        return givenName;
    }
}
