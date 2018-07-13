package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

public class BelfiusDateDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            return DateUtils.parseDate(BelfiusTokenReader.getTextValueAndAdvanceToken(p), "d/M/y");
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
