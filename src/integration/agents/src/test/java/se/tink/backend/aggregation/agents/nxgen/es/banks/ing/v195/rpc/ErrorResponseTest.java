package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ErrorResponseTest {
    private static String SINGLE_ERROR_RESPONSE_BODY =
            "{\"message\":\"Es necesario el alta en validación móvil\",\"errorCode\":19902}";
    private static String ARRAY_ERROR_RESPONSE_BODY =
            "{\"message\":[{\"field\":\"birthday\",\"message\":\"must match \\\"(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\\\d\\\\d)\\\"\",\"errorCode\":\"Pattern.birthday\"}]}";

    private <T> T loadTestResponse(String json, Class<T> cls) {
        return SerializationUtils.deserializeFromString(json, cls);
    }

    @Test
    public void testArrayErrorResponse() {
        final ErrorResponse errorResponse =
                loadTestResponse(ARRAY_ERROR_RESPONSE_BODY, ErrorResponse.class);
        assertTrue(errorResponse.hasErrorField("birthday"));
        assertTrue(errorResponse.hasErrorCode("Pattern.birthday"));
        assertEquals(1, errorResponse.getMessages().size());
    }

    @Test
    public void testSingleErrorResponse() {
        final ErrorResponse errorResponse =
                loadTestResponse(SINGLE_ERROR_RESPONSE_BODY, ErrorResponse.class);
        assertTrue(errorResponse.hasErrorMessage("Es necesario el alta en validación móvil"));
        assertTrue(errorResponse.hasErrorCode("19902"));
        assertEquals(1, errorResponse.getMessages().size());
    }
}
