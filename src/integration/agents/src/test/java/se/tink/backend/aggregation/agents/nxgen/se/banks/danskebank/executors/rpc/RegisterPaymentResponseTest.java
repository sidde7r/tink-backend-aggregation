package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RegisterPaymentResponseTest {

    @Test
    public void shouldReturnTransferExceptionWhenResponseMessageExistButNotKnown() {
        RegisterPaymentResponse registerPaymentResponse =
                setUpResponseWithMessage("\"Errormessage\"", "200");
        Throwable throwable = catchThrowable(registerPaymentResponse::validate);
        assertThat(throwable)
                .isExactlyInstanceOf(TransferExecutionException.class)
                .hasMessage("Transfer rejected by the Bank");
    }

    @Test
    public void shouldReturnTransferExceptionWhenResponseMessageExistAndKnown() {
        RegisterPaymentResponse registerPaymentResponse =
                setUpResponseWithMessage("\"Betalningen kan inte utföras i dag.\"", "200");
        Throwable throwable = catchThrowable(registerPaymentResponse::validate);
        assertThat(throwable)
                .isExactlyInstanceOf(TransferExecutionException.class)
                .hasMessage("The payment date is too soon or not a business day");
    }

    @Test
    public void shouldReturnTransferExceptionWhenStatusCodeNotOk() {
        RegisterPaymentResponse registerPaymentResponse = setUpResponseWithMessage(null, "500");
        Throwable throwable = catchThrowable(registerPaymentResponse::validate);
        assertThat(throwable)
                .isExactlyInstanceOf(TransferExecutionException.class)
                .hasMessage("Transfer rejected by the Bank");
    }

    @Test
    public void shouldReturnResponseWhenIsOkStatusAndResponseMessageNull() {
        RegisterPaymentResponse registerPaymentResponse = setUpResponseWithMessage(null, "200");
        RegisterPaymentResponse result = registerPaymentResponse.validate();
        assertNull(result.getResponseMessage());
        assertEquals(200, result.getStatusCode());
    }

    @Test
    public void shouldReturnResponseWhenIsOkStatusAndResponseMessageEmpty() {
        RegisterPaymentResponse registerPaymentResponse = setUpResponseWithMessage("\"\"", "200");
        RegisterPaymentResponse result = registerPaymentResponse.validate();
        assertEquals("", result.getResponseMessage());
        assertEquals(200, result.getStatusCode());
    }

    private RegisterPaymentResponse setUpResponseWithMessage(String message, String code) {
        return SerializationUtils.deserializeFromString(
                "{\"SignatureId\":null,\"InitSignPackage\":\"\",\"SignDataEnc\":\"\",\"UserID\":\"\",\"IsConfirmation\":true,\"ValidationResponse\":\"STRING\",\"AutoStartToken\":null,\"OrderRef\":null,\"HelperUrl\":null,\"ClientId\":null,\"ValidationMessage\":null,\"ForcableErrorsRC\":null,\"ForcableError\":null,\"SignatureText\":null,\"ResponseData\":null,\"AsheSignatureUrl\":null,\"EupToken\":null,\"ResponseMessage\": "
                        + message
                        + ",\"ResponseCode\":200,\"StatusCode\":"
                        + code
                        + ",\"TraceId\":null}",
                RegisterPaymentResponse.class);
    }
}
