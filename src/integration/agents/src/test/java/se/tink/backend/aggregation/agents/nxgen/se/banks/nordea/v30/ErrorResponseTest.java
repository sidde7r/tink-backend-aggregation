package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.rpc.ErrorResponse;
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

    private static final String INVALID_BANKGIRO_ACCOUNT_RESPONSE =
            "{\n"
                    + "\"http_status\": 400,\n"
                    + "\"error\": \"BESE1008\",\n"
                    + "\"error_description\": \"Invalid bankgiro account\",\n"
                    + "\"details\": [{\n"
                    + "\"more_info\": \"bad_request: Invalid bankgiro account\",\n"
                    + "\"reason\": \"BESE1008\"\n"
                    + "}]\n"
                    + "}";

    private static final String PAYMENT_NOT_FOUND_IN_OUTBOX =
            "{\n"
                    + "\"http_status\": 404,\n"
                    + "\"error\": \"not_found\",\n"
                    + "\"error_description\": \"Payment not found\",\n"
                    + "\"details\": [{\n"
                    + "\"param\": \"000000000215909872\"\n"
                    + "}]\n"
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

    @Test
    public void testErrorInvalidBankgiroAccount() {
        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(
                        INVALID_BANKGIRO_ACCOUNT_RESPONSE, ErrorResponse.class);
        Assert.assertTrue(errorResponse.isInvalidBankgiroAccount());
    }

    @Test
    public void testInvalidBankgiroErrorThrowsInvalidDestException() {
        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(
                        INVALID_BANKGIRO_ACCOUNT_RESPONSE, ErrorResponse.class);
        Throwable thrown = catchThrowable(errorResponse::throwAppropriateErrorIfAny);
        assertThat(thrown).isInstanceOf(TransferExecutionException.class);
        assertThat(((TransferExecutionException) thrown).getUserMessage())
                .isEqualTo("Invalid destination account");
        assertThat(((TransferExecutionException) thrown).getInternalStatus().toString())
                .isEqualTo("INVALID_DESTINATION_ACCOUNT");
    }

    @Test
    public void testPaymentNotFoundInOutbox() {
        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(
                        PAYMENT_NOT_FOUND_IN_OUTBOX, ErrorResponse.class);
        Assert.assertTrue(errorResponse.isPaymentNotFoundInOutbox());
    }
}
