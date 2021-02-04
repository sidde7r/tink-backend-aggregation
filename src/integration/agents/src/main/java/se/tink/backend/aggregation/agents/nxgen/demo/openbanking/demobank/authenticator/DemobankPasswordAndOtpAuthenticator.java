package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class DemobankPasswordAndOtpAuthenticator implements MultiFactorAuthenticator {
    private DemobankApiClient apiClient;
    private SupplementalInformationController supplementalInformationController;

    public DemobankPasswordAndOtpAuthenticator(
            DemobankApiClient apiClient,
            SupplementalInformationController supplementalInformationController) {
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String message =
                this.apiClient
                        .initEmbeddedOtp(
                                credentials.getField(Key.USERNAME),
                                credentials.getField(Key.PASSWORD))
                        .getMessage();
        final String otp;
        if ("select".equals(credentials.getField("otpmethod"))) {
            List<SelectOption> selectOptions = new ArrayList<>();
            for (int ii = 0; ii < 10; ii++) {
                selectOptions.add(new SelectOption(Integer.toString(ii), Integer.toString(ii)));
            }
            StringBuilder sb = new StringBuilder();
            for (int ii = 0; ii < 4; ii++) {
                String name = "digit" + (ii + 1);
                Field field =
                        Field.builder()
                                .description("OTP digit " + (ii + 1) + " " + message)
                                .helpText(message)
                                .immutable(true)
                                .masked(false)
                                .name(name)
                                .numeric(true)
                                .selectOptions(selectOptions)
                                .build();

                sb.append(
                        Optional.ofNullable(
                                        supplementalInformationController
                                                .askSupplementalInformationSync(field)
                                                .get(name))
                                .orElse(""));
            }
            otp = sb.toString();
        } else {
            otp =
                    supplementalInformationController
                            .askSupplementalInformationSync(
                                    Field.builder()
                                            .description("OTP Code")
                                            .helpText(message)
                                            .immutable(true)
                                            .masked(false)
                                            .name("otpinput")
                                            .numeric(true)
                                            .build())
                            .get("otpinput");
        }

        OAuth2Token token =
                this.apiClient
                        .completeEmbeddedOtp(
                                credentials.getField(Key.USERNAME),
                                credentials.getField(Key.PASSWORD),
                                otp)
                        .toOAuth2Token();

        this.apiClient.setTokenToStorage(token);
        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        token,
                        DemobankConstants.DEFAULT_OB_TOKEN_LIFETIME,
                        DemobankConstants.DEFAULT_OB_TOKEN_LIFETIME_UNIT));
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
