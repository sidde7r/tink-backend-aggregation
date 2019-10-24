package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DateEntity {

    @JsonProperty("valor")
    private String value;

    public static DateEntity of(LocalDate date) {
        DateEntity entity = new DateEntity();
        entity.setDate(date);
        return entity;
    }

    @JsonIgnore
    public LocalDate getLocalDate() {
        return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(value));
    }

    @JsonIgnore
    public void setDate(Date date) {
        setDate(date.toInstant().atZone(BankiaConstants.ZONE_ID).toLocalDate());
    }

    @JsonIgnore
    public void setDate(LocalDate date) {
        this.value = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    @JsonIgnore
    public Date toJavaLangDate() {
        return new Date(
                getLocalDate().atStartOfDay(BankiaConstants.ZONE_ID).toInstant().toEpochMilli());
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
