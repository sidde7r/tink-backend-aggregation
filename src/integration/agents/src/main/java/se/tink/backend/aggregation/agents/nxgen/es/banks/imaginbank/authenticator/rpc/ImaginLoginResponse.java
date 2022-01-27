package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ImaginLoginResponse {

    @JsonProperty("nombre")
    private String name;

    @JsonProperty("fechaNacimiento")
    private String dateOfBirth;

    @JsonProperty("flagEnrolamientoDispositivo")
    private String enrollmentIndicator;

    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEnrollmentIndicator() {
        return enrollmentIndicator;
    }
}
