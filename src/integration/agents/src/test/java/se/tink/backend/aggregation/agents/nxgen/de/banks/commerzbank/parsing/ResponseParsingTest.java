package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.ApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.CompleteAppRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.FinaliseApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitAppRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitScaResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.PrepareApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.SendTokenResponse;

public class ResponseParsingTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void successfulManualLogin() throws IOException {
        String loginResponseString =
                "{\"error\":null,\"metaData\":null,\"result\":{\"data\":{\"biometricKillSwitchesActive\":[],\"challenge\":null,\"firstName\":\"Kalle\",\"loginStatus\":\"TAN_REQUESTED\",\"sessionToken\":null,\"surname\":\"Kula\",\"userIdHash\":\"asdf\",\"userid\":\"12345678\"}}}";

        LoginResponse loginResponse = MAPPER.readValue(loginResponseString, LoginResponse.class);

        assertNull(loginResponse.getError());
        assertEquals(loginResponse.getLoginInfoEntity().getLoginStatus(), "TAN_REQUESTED");
    }

    @Test
    public void unsuccessfulManualLogin() throws IOException {
        String loginResponseString =
                "{\"error\":{\"cancelling\":true,\"errors\":[{\"messageId\":\"login.pin.error.10205\"}]},\"metaData\":{\"globalRequestId\":null,\"processContextId\":null},\"result\":null}";

        LoginResponse loginResponse = MAPPER.readValue(loginResponseString, LoginResponse.class);

        assertNotNull(loginResponse.getError());
    }

    @Test
    public void successfulInitScaResponse() throws IOException {
        String initScaResponseString =
                "{\"error\":null,\"metaData\":{\"globalRequestId\":\"asdf\",\"processContextId\":\"UUID\"},\"result\":{\"data\":{\"availableApprovalMethods\":[\"APP2APP_PHOTO_TAN\",\"PHOTO_TAN\"],\"salutationName\":\"Kula\",\"salutationTitle\":\"Herrn \"},\"hints\":[]}}";

        InitScaResponse initScaResponse =
                MAPPER.readValue(initScaResponseString, InitScaResponse.class);

        assertTrue(initScaResponse.getInitScaEntity().isPushPhotoTanAvailable());
    }

    @Test
    public void successfulPrepareApprovalResponse() throws IOException {
        String prepareApprovalResponseString =
                "{\"error\":null,\"metaData\":{\"globalRequestId\":null,\"processContextId\":\"UUID\"},\"result\":{\"data\":{\"approvalMethod\":\"PHOTO_TAN\",\"imageBase64\":\"The QR code image\",\"mobileNumber\":null,\"serverChallenge\":null},\"hints\":[]}}";

        PrepareApprovalResponse prepareApprovalResponse =
                MAPPER.readValue(prepareApprovalResponseString, PrepareApprovalResponse.class);

        assertEquals(
                prepareApprovalResponse.getPrepareApprovalEntity().getImageBase64(),
                "The QR code image");
    }

    @Test
    public void successfulApproveResponse() throws IOException {
        String approvalResponseString =
                "{\"error\":null,\"metaData\":{\"globalRequestId\":null,\"processContextId\":\"UUID\"},\"result\":{\"data\":{\"approval\":\"OK\"},\"hints\":[]}}";

        ApprovalResponse approvalResponse =
                MAPPER.readValue(approvalResponseString, ApprovalResponse.class);

        assertTrue(approvalResponse.getStatusEntity().isApprovalOk());
    }

    @Test
    public void successfulFinishApproveResponse() throws IOException {
        String finishApprovalResponseString =
                "{\"error\":null,\"metaData\":{\"globalRequestId\":null,\"processContextId\":null},\"result\":{\"data\":{\"loginStatus\":\"OK\"},\"hints\":[]}}";

        FinaliseApprovalResponse finaliseApprovalResponse =
                MAPPER.readValue(finishApprovalResponseString, FinaliseApprovalResponse.class);

        assertTrue(finaliseApprovalResponse.getStatusEntity().isLoginStatusOk());
    }

    @Test
    public void successfulInitAppRegistrationResponse() throws IOException {
        String initAppRegistrationResponseString =
                "{\"error\":null,\"result\":{\"items\":[{\"appId\":\"appID as UUID\"}],\"metaData\":null}}";

        InitAppRegistrationResponse initAppRegistrationResponse =
                MAPPER.readValue(
                        initAppRegistrationResponseString, InitAppRegistrationResponse.class);

        assertEquals(initAppRegistrationResponse.getAppId(), "appID as UUID");
    }

    @Test(expected = IllegalStateException.class)
    public void assertThatMoreThanOneAppIdListElementFails() throws IOException {
        String initAppRegistrationResponseString =
                "{\"error\":null,\"result\":{\"items\":[{\"appId\":\"appID as UUID\"},{\"someUnknownElement\":\"What is this even.\"}],\"metaData\":null}}";

        InitAppRegistrationResponse initAppRegistrationResponse =
                MAPPER.readValue(
                        initAppRegistrationResponseString, InitAppRegistrationResponse.class);

        initAppRegistrationResponse.getAppId();
    }

    @Test
    public void successfulCompleteAppRegistrationResponse() throws IOException {
        String completeAppRegistrationResponseString =
                "{\"error\":null,\"result\":{\"items\":[{\"appId\":\"appID as UUID\",\"appRegistrationData\":{\"profileJsonSettings\":null}}],\"metaData\":null}}";

        CompleteAppRegistrationResponse completeAppRegistrationResponse =
                MAPPER.readValue(
                        completeAppRegistrationResponseString,
                        CompleteAppRegistrationResponse.class);

        assertNull(completeAppRegistrationResponse.getError());
    }

    @Test
    public void successfulSendTwoFactorTokenResponse() throws IOException {
        String sendtwoFactorTokenResponseString =
                "{\"error\":null,\"metaData\":{\"globalRequestId\":null,\"processContextId\":null},\"result\":{\"data\":{\"status\":\"OK\"},\"hints\":[]}}";

        SendTokenResponse sendTokenResponse =
                MAPPER.readValue(sendtwoFactorTokenResponseString, SendTokenResponse.class);

        assertTrue(sendTokenResponse.getStatusEntity().isStatusOk());
    }
}
