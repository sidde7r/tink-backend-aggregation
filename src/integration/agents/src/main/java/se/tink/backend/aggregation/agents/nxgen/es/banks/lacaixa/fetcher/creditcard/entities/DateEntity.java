package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DateEntity {
    private static final Logger log = LoggerFactory.getLogger(DateEntity.class);

    @JsonProperty("valor")
    private String value;

    @JsonProperty("formato")
    private String format;

    public String getValue() {
        return value;
    }

    public Date getDate() {
        try {
            return DateEntity.parseDate(value, format);
        } catch (ParseException e) {
            log.error("Cannot parse date {} with format {}", value, format, e);
            return null;
        }
    }

    public static Date parseDate(String value, String format) throws ParseException {
        // only seen format as "DDMMAAAA" (ie "ddMMyyyy")
        if (!"DDMMAAAA".equals(format)) {
            log.warn("Unexpected date format: {}", format);
        }
        final String dateFormat = format.replaceAll("D", "d").replaceAll("A", "y");
        return new SimpleDateFormat(dateFormat).parse(value);
    }
}
