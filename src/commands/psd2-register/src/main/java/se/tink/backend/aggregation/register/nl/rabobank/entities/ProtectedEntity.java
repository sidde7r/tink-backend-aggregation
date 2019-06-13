package se.tink.backend.aggregation.register.nl.rabobank.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class ProtectedEntity {
    private String alg;
    private List<String> x5c;

    public static ProtectedEntity create(final String qsealB64) {
        final ProtectedEntity entity = new ProtectedEntity();

        entity.alg = "RS256"; // Only alg currently supported
        entity.x5c = Collections.singletonList(qsealB64);

        return entity;
    }
}
