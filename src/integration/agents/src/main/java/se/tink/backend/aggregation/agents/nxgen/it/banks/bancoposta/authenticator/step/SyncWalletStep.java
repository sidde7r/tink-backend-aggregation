package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step;

import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.SendOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.common.rpc.SimpleRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@AllArgsConstructor
@Slf4j
public class SyncWalletStep implements AuthenticationStep {
    private final BancoPostaApiClient apiClient;
    private final Catalog catalog;
    private static final String OTP_NAME = "otpSync";
    private static final LocalizableKey DESCRIPTION = new LocalizableKey("SMS Code");
    private static final LocalizableKey HELPTEXT =
            new LocalizableKey("Enter 6 digits code from SMS");

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        String otp = request.getUserInputs().get(OTP_NAME);

        if (StringUtils.isNotBlank(otp)) {
            apiClient.sendSmsOTPWallet(new SendOtpRequest(otp));
            return AuthenticationStepResponse.executeStepWithId(
                    BancoPostaAuthenticator.REGSITER_VERIFICATION_STEP_ID);
        }

        apiClient.initSyncWallet(new SimpleRequest());
        apiClient.requestForSmsOtpWallet(new SimpleRequest());

        log.info("waiting for user to input SMS otp");
        return AuthenticationStepResponse.requestForSupplementInformation(
                new SupplementInformationRequester.Builder()
                        .withFields(Collections.singletonList(buildField()))
                        .build());
    }

    private Field buildField() {
        return Field.builder()
                .name(OTP_NAME)
                .description(catalog.getString(DESCRIPTION))
                .numeric(true)
                .minLength(6)
                .maxLength(6)
                .helpText(catalog.getString(HELPTEXT))
                .build();
    }

    @Override
    public String getIdentifier() {
        return BancoPostaAuthenticator.SYNC_WALLET_STEP_ID;
    }
}
