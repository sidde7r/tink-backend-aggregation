package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ErrorResponseTest {
    private static final String INVALID_OCR_BANK_RESPONSE =
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

    private static final String USER_UNAUTHORIZED_BANK_RESPONSE =
            "{\n"
                    + "    \"http_status\": 403,\n"
                    + "    \"error\": \"error_core_unauthorized\",\n"
                    + "    \"error_description\": \"User not authorised to operation\",\n"
                    + "    \"details\": [\n"
                    + "        {\n"
                    + "            \"more_info\": \"No permission to create payment\"\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    @Test
    public void testErrorResponseDeser() {
        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(
                        INVALID_OCR_BANK_RESPONSE, ErrorResponse.class);
        Assert.assertTrue(errorResponse.isInvalidOcr());
    }

    @Test
    public void testErrorUserUnauthorizedError() {
        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(
                        USER_UNAUTHORIZED_BANK_RESPONSE, ErrorResponse.class);
        Assert.assertTrue(errorResponse.isUserUnauthorizedError());
    }
}
