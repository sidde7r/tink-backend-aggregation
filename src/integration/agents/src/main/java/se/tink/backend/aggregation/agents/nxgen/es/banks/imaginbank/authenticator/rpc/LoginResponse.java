package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {

    @JsonIgnore
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @JsonProperty("nombre")
    private String name;

    @JsonProperty("fechaNacimiento")
    private String dateOfBirth;

    public String getName() {
        return name;
    }

    public LocalDate getFormattedDateOfBirth() {
        return LocalDate.parse(dateOfBirth, DATE_FORMATTER);
    }
}
