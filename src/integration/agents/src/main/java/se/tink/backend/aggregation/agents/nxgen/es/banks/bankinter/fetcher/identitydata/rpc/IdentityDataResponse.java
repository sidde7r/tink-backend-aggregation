package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.identitydata.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.libraries.identitydata.IdentityData;

public class IdentityDataResponse {
    private static final DateTimeFormatter BIRTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("ddMMyyyy");

    @JsonProperty("nombre")
    private String firstName;

    @JsonProperty("apellidos")
    private String surnames;

    @JsonProperty("indicadorSexo")
    private String gender;

    @JsonProperty("fechaNacimiento")
    private String birthDate;

    public IdentityData toIdentityData() {
        return IdentityData.builder()
                .addFirstNameElement(firstName)
                .addSurnameElement(surnames)
                .setDateOfBirth(LocalDate.parse(birthDate, BIRTH_DATE_FORMATTER))
                .build();
    }
}
