package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ErrorResponseTest {
    private static final String SOURCE =
            "{\n"
                    + "  \"http_status\": 400,\n"
                    + "  \"error\": \"BESE1009\",\n"
                    + "  \"error_description\": \"Error in reference number (OCR)\",\n"
                    + "  \"details\": [\n"
                    + "    {\n"
                    + "      \"more_info\": \"bad_request: Error in reference number (OCR)\",\n"
                    + "      \"reason\": \"BESE1009\"\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";

    @Test
    public void testErrorResponseDeser() {
        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(SOURCE, ErrorResponse.class);
        Assert.assertTrue(errorResponse.isInvalidOcr());
    }
}
