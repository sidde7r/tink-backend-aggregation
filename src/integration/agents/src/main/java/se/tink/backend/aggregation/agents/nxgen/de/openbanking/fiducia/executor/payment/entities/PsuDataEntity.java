package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PsuDataEntity {
    private String password;

    @JsonIgnore
    public PsuDataEntity(String password) {
        this.password = password;
    }
}
