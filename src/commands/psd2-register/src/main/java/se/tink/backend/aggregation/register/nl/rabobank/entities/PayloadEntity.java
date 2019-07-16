package se.tink.backend.aggregation.register.nl.rabobank.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class PayloadEntity {
    @JsonProperty("ptc_email")
    private String email;

    private String organization;
    private Integer exp;

    public static PayloadEntity create(
            final int exp, final String email, final String organization) {
        final PayloadEntity entity = new PayloadEntity();
        entity.email = email;
        entity.exp = exp;
        entity.organization = organization;
        return entity;
    }
}
