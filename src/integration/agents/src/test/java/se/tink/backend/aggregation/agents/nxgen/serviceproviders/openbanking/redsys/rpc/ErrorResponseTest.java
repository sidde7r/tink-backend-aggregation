package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.rpc;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ErrorResponseTest {

    @Test
    public void testMultipleErrorResponse() {
        final String json =
                "{\n"
                        + "    \"tppMessages\": [\n"
                        + "        {\n"
                        + "            \"category\": \"ERROR\",\n"
                        + "            \"code\": \"RESOURCE_UNKNOWN\",\n"
                        + "            \"text\": \"Resource Not Found\"\n"
                        + "        },{\n"
                        + "            \"category\": \"ERROR\",\n"
                        + "            \"code\": \"Internal Server Error\"\n"
                        + "        }"
                        + "    ]\n"
                        + "}";

        final ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(json, ErrorResponse.class);

        assertTrue(errorResponse.hasErrorCode("RESOURCE_UNKNOWN"));
        assertTrue(errorResponse.hasErrorCode("Internal Server Error"));
    }

    @Test
    public void testSingleErrorResponse() {
        final String json =
                "{\n"
                        + "    \"tppMessages\": [\n"
                        + "        {\n"
                        + "            \"category\": \"ERROR\",\n"
                        + "            \"code\": \"RESOURCE_UNKNOWN\",\n"
                        + "            \"text\": \"Resource Not Found\"\n"
                        + "        }"
                        + "    ]\n"
                        + "}";

        final ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(json, ErrorResponse.class);

        assertTrue(errorResponse.hasErrorCode("RESOURCE_UNKNOWN"));
    }

    @Test
    public void testTopLevelErrorResponse() {
        final String json =
                "{\n"
                        + "    \"category\": \"ERROR\",\n"
                        + "    \"code\": \"RESOURCE_UNKNOWN\",\n"
                        + "    \"text\": \"HTTP 404 Not Found\"\n"
                        + "}";

        final ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(json, ErrorResponse.class);

        assertTrue(errorResponse.hasErrorCode("RESOURCE_UNKNOWN"));
    }
}
