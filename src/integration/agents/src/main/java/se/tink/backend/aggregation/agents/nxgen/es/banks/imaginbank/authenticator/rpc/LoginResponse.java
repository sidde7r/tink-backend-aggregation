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

    @JsonProperty("ima")
    private String userName;

    @JsonProperty("resImagin")
    private ImaginLoginResponse imaginLoginResponse;

    @JsonIgnore
    public String getName() {
        return imaginLoginResponse.getName();
    }

    @JsonIgnore
    public String dateOfBirth() {
        return imaginLoginResponse.getDateOfBirth();
    }

    @JsonIgnore
    public LocalDate getFormattedDateOfBirth() {
        return LocalDate.parse(dateOfBirth(), DATE_FORMATTER);
    }

    public ImaginLoginResponse getImaginLoginResponse() {
        return imaginLoginResponse;
    }
}
