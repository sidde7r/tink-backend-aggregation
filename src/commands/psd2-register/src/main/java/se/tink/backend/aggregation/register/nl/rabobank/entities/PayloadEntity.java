package se.tink.backend.aggregation.register.nl.rabobank.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class PayloadEntity {
    private String ptc_email;
    private String organization;
    private Integer exp;

    public static PayloadEntity create(
            final int exp, final String email, final String organization) {
        final PayloadEntity entity = new PayloadEntity();
        entity.ptc_email = email;
        entity.exp = exp;
        entity.organization = organization;
        return entity;
    }
}
