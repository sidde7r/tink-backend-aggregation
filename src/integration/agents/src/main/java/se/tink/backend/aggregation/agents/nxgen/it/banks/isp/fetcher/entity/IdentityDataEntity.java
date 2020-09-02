package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;

@JsonObject
@Data
@Slf4j
public class IdentityDataEntity {

    // https://it.wikipedia.org/wiki/Codice_fiscale
    private static final Pattern FISCAL_CODE_PATTERN =
            Pattern.compile(".{6}(\\d{2})([A-Z])(\\d{2}).{5}");
    private static final Map<String, Integer> monthCodeToMonth = new HashMap<>();

    static {
        monthCodeToMonth.put("A", 1);
        monthCodeToMonth.put("B", 2);
        monthCodeToMonth.put("C", 3);
        monthCodeToMonth.put("D", 4);
        monthCodeToMonth.put("E", 5);
        monthCodeToMonth.put("H", 6);
        monthCodeToMonth.put("L", 7);
        monthCodeToMonth.put("M", 8);
        monthCodeToMonth.put("P", 9);
        monthCodeToMonth.put("R", 10);
        monthCodeToMonth.put("S", 11);
        monthCodeToMonth.put("T", 12);
    }

    @JsonProperty("codiceFiscale")
    private String fiscalCode;

    @JsonProperty("cognome")
    private String surname;

    @JsonProperty("nome")
    private String name;

    public IdentityData toTinkIdentityData() {

        Optional<LocalDate> dateOfBirth = fiscalCodeToDateOfBirth(fiscalCode);
        return IdentityData.builder()
                .addFirstNameElement(name)
                .addSurnameElement(surname)
                .setDateOfBirth(dateOfBirth.orElse(null))
                .build();
    }

    private static Optional<LocalDate> fiscalCodeToDateOfBirth(String fiscalCode) {
        Matcher matcher = FISCAL_CODE_PATTERN.matcher(fiscalCode);

        if (!matcher.matches()) {
            log.warn("Cannot decode fiscal code {}", fiscalCode);
            return Optional.empty();
        }

        String year2Digits = matcher.group(1);
        String monthCode = matcher.group(2);
        String dayAndSex = matcher.group(3);

        // guess the year based on 2 digits. Assuming client is between 1 and 100 years old.
        int year = 2000 + Integer.parseInt(year2Digits);
        if (year >= LocalDate.now().getYear()) {
            year -= 100;
        }

        int month = monthCodeToMonth.get(monthCode);

        int dayAndSexDecoded = Integer.parseInt(dayAndSex);
        int day = dayAndSexDecoded <= 31 ? dayAndSexDecoded : dayAndSexDecoded - 40;

        return Optional.of(LocalDate.of(year, month, day));
    }
}
