package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ErrorResponseTest {
    private static final String SERIALIZED_ERROR_RESPONSE_BODY_FOR_ERROR_MESSAGE =
            "{"
                    + "\"errorMessages\":{\""
                    + "general\":[],"
                    + "\"fields\":[{"
                    + "\"field\":\"reference\",\""
                    + "message\":\"Den valda mottagaren tillåter inte betalningar med OCR-nummer, vänligen välj meddelande.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    private static final String SERIALIZED_ERROR_RESPONSE_BODY_FOR_ERROR_CODE =
            "{"
                    + "\"errorMessages\":{"
                    + "\"general\":[{"
                    + "\"code\":\"NOT_ALLOWED\","
                    + "\"message\":\"Inloggningen med Mobilt BankID påbörjades inte på rätt sätt. Vänligen försök igen.\""
                    + "}"
                    + "]"
                    + "}"
                    + "}";

    @Test
    public void deserializeErrorMessage() {
        String errorMessage =
                "Den valda mottagaren tillåter inte betalningar med OCR-nummer, vänligen välj meddelande.";
        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(
                        SERIALIZED_ERROR_RESPONSE_BODY_FOR_ERROR_MESSAGE, ErrorResponse.class);

        assertNotNull(errorResponse.getErrorMessages());
        assertNotNull(errorResponse.getErrorMessages().getGeneral());
        assertNotNull(errorResponse.getErrorMessages().getFields());

        assertTrue(errorResponse.hasErrorField(SwedbankBaseConstants.ErrorField.REFERENCE));
        assertTrue(errorResponse.hasErrorMessage(errorMessage));
    }

    @Test
    public void deserializeErrorCode() {
        String errorMessage =
                "Inloggningen med Mobilt BankID påbörjades inte på rätt sätt. Vänligen försök igen.";
        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(
                        SERIALIZED_ERROR_RESPONSE_BODY_FOR_ERROR_CODE, ErrorResponse.class);

        assertNotNull(errorResponse.getErrorMessages());
        assertNotNull(errorResponse.getErrorMessages().getGeneral());

        assertNull(errorResponse.getErrorMessages().getFields());

        assertTrue(errorResponse.hasErrorCode(SwedbankBaseConstants.ErrorCode.NOT_ALLOWED));
        assertTrue(errorResponse.getAllErrors().contains(errorMessage));
    }
}
