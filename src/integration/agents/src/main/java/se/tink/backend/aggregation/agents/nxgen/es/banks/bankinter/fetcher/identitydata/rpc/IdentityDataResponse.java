package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.identitydata.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import se.tink.libraries.identitydata.IdentityData;

public class IdentityDataResponse {
    private static final DateTimeFormatter BIRTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String ssn;

    private String firstName;

    private String surnames;

    private String birthDate;

    @JsonProperty("personalData")
    private void getPersonalData(Map<String, String> personalData) {
        firstName = personalData.get("name");

        surnames = personalData.get("surnames");

        birthDate = personalData.get("birthdate");
    }

    @JsonProperty("identificationDoc")
    private void getIdentificationDoc(Map<String, String> identificationDoc) {
        ssn = identificationDoc.get("number");
    }

    public IdentityData toIdentityData() {
        return IdentityData.builder()
                .setSsn(ssn)
                .addFirstNameElement(firstName)
                .addSurnameElement(surnames)
                .setDateOfBirth(LocalDate.parse(birthDate, BIRTH_DATE_FORMATTER))
                .build();
    }
}
