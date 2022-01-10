package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.DATE_FORMAT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.DATE_REGEX_DDMMYYYY;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DateEntity {

    @JsonProperty("valor")
    private String value;

    @JsonProperty("formato")
    private String format;

    public LocalDate toTinkDate() {
        if (DATE_FORMAT.equals(format) && !value.matches(DATE_REGEX_DDMMYYYY)) {
            return null;
        }
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(format));
    }
}
