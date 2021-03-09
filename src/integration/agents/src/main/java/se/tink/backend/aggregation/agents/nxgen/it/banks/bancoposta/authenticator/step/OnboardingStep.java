package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RegisterCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RegisterInitCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.InitRegistrationWithDigitalCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegistrationWithDigitalCodeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@AllArgsConstructor
@Slf4j
public class OnboardingStep implements AuthenticationStep {
    private final BancoPostaApiClient apiClient;
    private final BancoPostaStorage storage;
    private final Catalog catalog;

    private static final String OTP_NAME = "otpOnboarding";
    private static final String ACCOUNT_CODE = "accountcode";
    private static final LocalizableKey DESCRIPTION = new LocalizableKey("SMS Code");
    private static final LocalizableKey HELPTEXT =
            new LocalizableKey("Enter 4 digits code from SMS");

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        String otp = request.getUserInputs().get(OTP_NAME);

        if (otp != null) {
            String accountCode = request.getCredentials().getField(ACCOUNT_CODE);
            registerWithDigitalCode(otp, accountCode);
            return AuthenticationStepResponse.executeStepWithId(
                    BancoPostaAuthenticator.REGSITER_VERIFICATION_STEP_ID);
        }

        initRegistrationWithDigitalCode();
        log.info("Waiting for user to input account code and sms otp");
        return AuthenticationStepResponse.requestForSupplementInformation(
                new SupplementInformationRequester.Builder()
                        .withFields(ImmutableList.of(buildOtpField()))
                        .build());
    }

    private void registerWithDigitalCode(String otp, String accountNumberCode) {
        RegisterCodeRequest requestBody =
                new RegisterCodeRequest(otp, accountNumberCode, storage.getAccountAlias());

        RegistrationWithDigitalCodeResponse registrationWithDigitalCodeResponse =
                apiClient.registerWithDigitalCode(requestBody);

        if (ErrorCodes.INCORRECT_CREDENTIALS.equals(
                registrationWithDigitalCodeResponse.getHeader().getCommandResultDescription())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String registerToken = registrationWithDigitalCodeResponse.getBody().getRegisterToken();
        storage.saveToPersistentStorage(Storage.REGISTER_TOKEN, registerToken);
    }

    private void initRegistrationWithDigitalCode() {
        InitRegistrationWithDigitalCodeResponse initRegistrationWithDigitalCodeResponse =
                apiClient.initAccountWithDigitalCode(
                        new RegisterInitCodeRequest(storage.getAccountNumber()));
        String accountAlias = initRegistrationWithDigitalCodeResponse.getBody().getAccountAlias();
        storage.saveToPersistentStorage(Storage.ACCOUNT_ALIAS, accountAlias);
    }

    private Field buildOtpField() {
        return Field.builder()
                .name(OTP_NAME)
                .description(catalog.getString(DESCRIPTION))
                .numeric(true)
                .minLength(4)
                .maxLength(4)
                .helpText(catalog.getString(HELPTEXT))
                .build();
    }

    @Override
    public String getIdentifier() {
        return BancoPostaAuthenticator.ONBOARDING_STEP_ID;
    }
}
