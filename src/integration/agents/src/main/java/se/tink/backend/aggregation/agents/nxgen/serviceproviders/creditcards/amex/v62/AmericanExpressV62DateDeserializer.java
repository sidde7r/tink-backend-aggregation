package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmericanExpressV62DateDeserializer extends JsonDeserializer<Date> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AmericanExpressV62DateDeserializer.class);

    private static JsonNode contentNode(JsonParser jsonParser) throws IOException {
        ObjectCodec objectCodec = jsonParser.getCodec();
        JsonNode node = objectCodec.readTree(jsonParser);
        JsonNode contentNode = node.get("rawValue");
        return contentNode;
    }

    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        JsonNode jsonNode = contentNode(jsonParser);
        if (jsonNode == null) {
            return null;
        }
        String dateString = jsonNode.asText();

        if (Strings.isNullOrEmpty(dateString)) {
            return null;
        }
        Date date;
        try {
            date =
                    DateUtils.flattenTime(
                            DateUtils.flattenTime(
                                    ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(dateString)));
        } catch (ParseException e) {
            String errorMessage = "Cannot parse date: " + dateString;
            LOGGER.error(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
        return date;
    }
}
