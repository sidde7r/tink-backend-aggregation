package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentNameBody {
    @JsonProperty("Name")
    private String name;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public void setName(String name) {
        this.name = name;
    }
}
