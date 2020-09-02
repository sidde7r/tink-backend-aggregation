package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RequestBody;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.InitRegistrationWithDigitalCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegistrationWithDigitalCodeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.libraries.i18n.Catalog;

@AllArgsConstructor
@Slf4j
public class OnboardingStep implements AuthenticationStep {
    private BancoPostaApiClient apiClient;
    private UserContext userContext;
    private Catalog catalog;

    private static final String OTP_NAME = "otpOnboarding";
    private static final String CODE_NAME = "codeDigital";

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        String otp = request.getUserInputs().get(OTP_NAME);
        String codeDigital = request.getUserInputs().get(CODE_NAME);

        if (Objects.nonNull((otp)) && Objects.nonNull((codeDigital))) {
            registerWithDigitalCode(otp, codeDigital);
            return AuthenticationStepResponse.executeStepWithId(
                    BancoPostaAuthenticator.REGSITER_VERIFICATION_STEP_ID);
        }

        initRegistrationWithDigitalCode();
        log.info("Waiting for user to input account code and sms otp");
        return AuthenticationStepResponse.requestForSupplementInformation(
                new SupplementInformationRequester.Builder()
                        .withFields(ImmutableList.of(buildCodeDigitalField(), buildOtpField()))
                        .build());
    }

    private void registerWithDigitalCode(String otp, String digitalCode) {
        RequestBody requestBody = generateRegisterRequestBody(otp, digitalCode);
        RegistrationWithDigitalCodeResponse registrationWithDigitalCodeResponse =
                apiClient.registerWithDigitalCode(requestBody);

        if ("WS_CALL_ERROR"
                .equals(
                        registrationWithDigitalCodeResponse
                                .getHeader()
                                .getCommandResultDescription())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String registerToken = registrationWithDigitalCodeResponse.getBody().getRegisterToken();
        userContext.setRegisterToken(registerToken);
    }

    private RequestBody generateRegisterRequestBody(String otp, String digitalCode) {
        ImmutableMap<String, String> body =
                ImmutableMap.<String, String>builder()
                        .put("smsOTP", otp)
                        .put("codiceDigital", digitalCode)
                        .put("aliasWallet", userContext.getAccountAlias())
                        .build();
        return new RequestBody(body);
    }

    private void initRegistrationWithDigitalCode() {
        InitRegistrationWithDigitalCodeResponse initRegistrationWithDigitalCodeResponse =
                apiClient.initAccountWithDigitalCode(generateRegisterInitRequestBody());
        String accountAlias = initRegistrationWithDigitalCodeResponse.getBody().getAccountAlias();
        userContext.setAccountAlias(accountAlias);
    }

    private RequestBody generateRegisterInitRequestBody() {
        ImmutableMap<String, String> body =
                ImmutableMap.of("numeroConto", userContext.getAccountNumber());
        return new RequestBody(body);
    }

    private Field buildOtpField() {
        return Field.builder()
                .name(OTP_NAME)
                .immutable(true)
                .description(catalog.getString("SMS OTP"))
                .numeric(true)
                .minLength(4)
                .maxLength(4)
                .helpText(catalog.getString("Enter 4 digits code from SMS"))
                .build();
    }

    private Field buildCodeDigitalField() {
        return Field.builder()
                .name(CODE_NAME)
                .immutable(true)
                .description(catalog.getString("account number"))
                .numeric(true)
                .minLength(4)
                .maxLength(4)
                .helpText(
                        catalog.getString(
                                "Enter 4 digit account number, can be found in aggreement"))
                .build();
    }

    @Override
    public String getIdentifier() {
        return BancoPostaAuthenticator.ONBOARDING_STEP_ID;
    }
}
