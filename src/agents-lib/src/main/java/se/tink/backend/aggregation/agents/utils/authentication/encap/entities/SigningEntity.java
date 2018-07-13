package se.tink.backend.aggregation.agents.utils.authentication.encap.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SigningEntity {
    private Object csr;

    public Object getCsr() {
        return csr;
    }
}
