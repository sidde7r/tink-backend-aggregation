package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RequestBody;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.libraries.i18n.Catalog;

@AllArgsConstructor
@Slf4j
public class SyncWalletStep implements AuthenticationStep {
    private BancoPostaApiClient apiClient;
    private Catalog catalog;
    private static final String OTP_NAME = "otpSync";

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        String otp = request.getUserInputs().get(OTP_NAME);

        if (Objects.nonNull(otp)) {
            apiClient.sendSmsOTPWallet(generateRequestBody(otp));
            return AuthenticationStepResponse.executeStepWithId(
                    BancoPostaAuthenticator.REGSITER_VERIFICATION_STEP_ID);
        }

        apiClient.initSyncWallet(new RequestBody(new HashMap<>()));
        apiClient.requestForSmsOtpWallet(new RequestBody(new HashMap<>()));

        log.info("waiting for user to input SMS otp");
        return AuthenticationStepResponse.requestForSupplementInformation(
                new SupplementInformationRequester.Builder()
                        .withFields(Collections.singletonList(buildField()))
                        .build());
    }

    private RequestBody generateRequestBody(String otp) {
        Map<String, String> body = new HashMap<>();
        body.put("smsOTP", otp);
        return new RequestBody(body);
    }

    private Field buildField() {
        return Field.builder()
                .name(OTP_NAME)
                .immutable(true)
                .description(catalog.getString("SMS Code"))
                .numeric(true)
                .minLength(6)
                .maxLength(6)
                .helpText(catalog.getString("Enter 6 digits code from SMS"))
                .build();
    }

    @Override
    public String getIdentifier() {
        return BancoPostaAuthenticator.SYNC_WALLET_STEP_ID;
    }
}
