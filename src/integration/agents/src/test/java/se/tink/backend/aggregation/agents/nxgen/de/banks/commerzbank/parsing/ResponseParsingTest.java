package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.parsing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.ApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.CompleteAppRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.FinaliseApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitAppRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitScaResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.PrepareApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.SendTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ResponseParsingTest {

    @Test
    public void successfulManualLogin() {
        // given
        String loginResponseString =
                "{\"error\":null,\"metaData\":null,\"result\":{\"data\":{\"biometricKillSwitchesActive\":[],\"challenge\":null,\"firstName\":\"Kalle\",\"loginStatus\":\"TAN_REQUESTED\",\"sessionToken\":null,\"surname\":\"Kula\",\"userIdHash\":\"asdf\",\"userid\":\"12345678\"}}}";

        // when
        LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(loginResponseString, LoginResponse.class);

        // then
        assertThat(loginResponse.getError()).isNull();
        assertThat(loginResponse.getLoginInfoEntity().getLoginStatus()).isEqualTo("TAN_REQUESTED");
    }

    @Test
    public void unsuccessfulManualLogin() {
        // given
        String loginResponseString =
                "{\"error\":{\"cancelling\":true,\"errors\":[{\"messageId\":\"login.pin.error.10205\"}]},\"metaData\":{\"globalRequestId\":null,\"processContextId\":null},\"result\":null}";

        // when
        LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(loginResponseString, LoginResponse.class);

        // then
        assertThat(loginResponse.getError()).isNotNull();
    }

    @Test
    public void successfulInitScaResponse() {
        // given
        String initScaResponseString =
                "{\"error\":null,\"metaData\":{\"globalRequestId\":\"asdf\",\"processContextId\":\"UUID\"},\"result\":{\"data\":{\"availableApprovalMethods\":[\"APP2APP_PHOTO_TAN\",\"PUSH_PHOTO_TAN\"],\"salutationName\":\"Kula\",\"salutationTitle\":\"Herrn \"},\"hints\":[]}}";

        // when
        InitScaResponse initScaResponse =
                SerializationUtils.deserializeFromString(
                        initScaResponseString, InitScaResponse.class);

        // then
        assertThat(initScaResponse.getInitScaEntity().isPushPhotoTanAvailable()).isTrue();
    }

    @Test
    public void successfulPrepareApprovalResponse() {
        // given
        String prepareApprovalResponseString =
                "{\"error\":null,\"metaData\":{\"globalRequestId\":null,\"processContextId\":\"UUID\"},\"result\":{\"data\":{\"approvalMethod\":\"PHOTO_TAN\",\"imageBase64\":\"The QR code image\",\"mobileNumber\":null,\"serverChallenge\":null},\"hints\":[]}}";

        // when
        PrepareApprovalResponse prepareApprovalResponse =
                SerializationUtils.deserializeFromString(
                        prepareApprovalResponseString, PrepareApprovalResponse.class);

        // then
        assertThat(prepareApprovalResponse.getPrepareApprovalEntity().getImageBase64())
                .isEqualTo("The QR code image");
    }

    @Test
    public void successfulApproveResponse() {
        // given
        String approvalResponseString =
                "{\"error\":null,\"metaData\":{\"globalRequestId\":null,\"processContextId\":\"UUID\"},\"result\":{\"data\":{\"approval\":\"OK\"},\"hints\":[]}}";

        // when
        ApprovalResponse approvalResponse =
                SerializationUtils.deserializeFromString(
                        approvalResponseString, ApprovalResponse.class);

        // then
        assertThat(approvalResponse.getStatusEntity().isApprovalOk()).isTrue();
    }

    @Test
    public void successfulFinishApproveResponse() {
        // given
        String finishApprovalResponseString =
                "{\"error\":null,\"metaData\":{\"globalRequestId\":null,\"processContextId\":null},\"result\":{\"data\":{\"loginStatus\":\"OK\"},\"hints\":[]}}";

        // when
        FinaliseApprovalResponse finaliseApprovalResponse =
                SerializationUtils.deserializeFromString(
                        finishApprovalResponseString, FinaliseApprovalResponse.class);

        // then
        assertThat(finaliseApprovalResponse.getStatusEntity().isLoginStatusOk()).isTrue();
    }

    @Test
    public void successfulInitAppRegistrationResponse() {
        // given
        String initAppRegistrationResponseString =
                "{\"error\":null,\"result\":{\"items\":[{\"appId\":\"appID as UUID\"}],\"metaData\":null}}";

        // when
        InitAppRegistrationResponse initAppRegistrationResponse =
                SerializationUtils.deserializeFromString(
                        initAppRegistrationResponseString, InitAppRegistrationResponse.class);

        // then
        assertThat(initAppRegistrationResponse.getAppId()).isEqualTo("appID as UUID");
    }

    @Test
    public void assertThatMoreThanOneAppIdListElementFails() {
        // given
        String initAppRegistrationResponseString =
                "{\"error\":null,\"result\":{\"items\":[{\"appId\":\"appID as UUID\"},{\"someUnknownElement\":\"What is this even.\"}],\"metaData\":null}}";
        // and
        InitAppRegistrationResponse initAppRegistrationResponse =
                SerializationUtils.deserializeFromString(
                        initAppRegistrationResponseString, InitAppRegistrationResponse.class);

        // when
        Throwable t = catchThrowable(initAppRegistrationResponse::getAppId);

        // then
        assertThat(t)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not get appId which is required for registration.");
    }

    @Test
    public void successfulCompleteAppRegistrationResponse() {
        // given
        String completeAppRegistrationResponseString =
                "{\"error\":null,\"result\":{\"items\":[{\"appId\":\"appID as UUID\",\"appRegistrationData\":{\"profileJsonSettings\":null}}],\"metaData\":null}}";
        // and
        CompleteAppRegistrationResponse completeAppRegistrationResponse =
                SerializationUtils.deserializeFromString(
                        completeAppRegistrationResponseString,
                        CompleteAppRegistrationResponse.class);

        // when
        ErrorEntity result = completeAppRegistrationResponse.getError();

        // then
        assertThat(result).isNull();
    }

    @Test
    public void successfulSendTwoFactorTokenResponse() {
        // given
        String sendtwoFactorTokenResponseString =
                "{\"error\":null,\"metaData\":{\"globalRequestId\":null,\"processContextId\":null},\"result\":{\"data\":{\"status\":\"OK\"},\"hints\":[]}}";
        // and
        SendTokenResponse sendTokenResponse =
                SerializationUtils.deserializeFromString(
                        sendtwoFactorTokenResponseString, SendTokenResponse.class);

        // when
        boolean result = sendTokenResponse.getStatusEntity().isStatusOk();

        // then
        assertThat(result).isTrue();
    }
}
