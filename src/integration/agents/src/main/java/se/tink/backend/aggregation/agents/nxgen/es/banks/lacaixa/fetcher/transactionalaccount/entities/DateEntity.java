package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

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
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(format));
    }
}
