package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    public LocalDate toTinkDate() {
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(format));
    }
}
