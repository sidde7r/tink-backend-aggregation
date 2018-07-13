package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer;

import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;

class BelfiusTokenReader {
    static String getTextValueAndAdvanceToken(JsonParser p) throws IOException {
        p.nextFieldName();
        String value = p.nextTextValue();
        p.nextToken();
        return value;
    }
}
