package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
public class ConsentDetailsResponse {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate validUntil;

    public LocalDate getValidUntil() {
        return validUntil;
    }
}
