package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorResponse;

public class ErrorResponseTest {

    @Test
    public void testHasErrorField() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String source =
                "{\"errorMessages\":{\"general\":[],\"fields\":[{\"field\":\"reference\",\"message\":\"Den valda mottagaren tillåter inte betalningar med OCR-nummer, vänligen välj meddelande.\"}]}}";
        ErrorResponse errorMessagesEntity = objectMapper.readValue(source, ErrorResponse.class);
        Assert.assertTrue(errorMessagesEntity.hasErrorField("reference"));
    }
}
