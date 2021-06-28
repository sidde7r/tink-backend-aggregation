package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.sdktemplates.TemplatesSupplementalInfoBuilder;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RequiredArgsConstructor
public class DemobankPasswordAnd2FAWithTemplatesAuthenticator implements MultiFactorAuthenticator {
    private final DemobankApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String message =
                this.apiClient
                        .initEmbeddedOtp(
                                credentials.getField(Key.USERNAME),
                                credentials.getField(Key.PASSWORD))
                        .getMessage();

        String otpCode = handleSupplementalInformationFlow(message);

        OAuth2Token token =
                this.apiClient
                        .completeEmbeddedOtp(
                                credentials.getField(Key.USERNAME),
                                credentials.getField(Key.PASSWORD),
                                otpCode)
                        .toOAuth2Token();

        this.apiClient.setTokenToStorage(token);
        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        token,
                        DemobankConstants.DEFAULT_OB_TOKEN_LIFETIME,
                        DemobankConstants.DEFAULT_OB_TOKEN_LIFETIME_UNIT));
    }

    private String handleSupplementalInformationFlow(String message) {
        String name = "2fa-option";
        Field field = TemplatesSupplementalInfoBuilder.createTemplateSelectOption(message, name);
        TemplatesSupplementalInfoBuilder.TemplateType chosen2faOption =
                askToChoose2FAMethod(name, field);
        String otpCode = getOtpCode(message);

        List<Field> fields =
                TemplatesSupplementalInfoBuilder.createTemplateSupplementalInfo(
                        chosen2faOption, otpCode);

        Map<String, String> supplementalInfoResponse =
                supplementalInformationController.askSupplementalInformationSync(
                        fields.toArray(new Field[0]));

        if (shouldHandlePossibleChangeMethod(supplementalInfoResponse)) {
            handleSupplementalInformationFlow(message);
        }

        return otpCode;
    }

    private TemplatesSupplementalInfoBuilder.TemplateType askToChoose2FAMethod(
            String name, Field field) {
        return TemplatesSupplementalInfoBuilder.TemplateType.valueOf(
                Optional.ofNullable(
                                supplementalInformationController
                                        .askSupplementalInformationSync(field)
                                        .get(name))
                        .orElseThrow(() -> new IllegalStateException("Invalid 2FA method passed")));
    }

    private String getOtpCode(String message) {
        return message.replaceAll("\\D+", "");
    }

    private boolean shouldHandlePossibleChangeMethod(Map<String, String> supplementalInfoResponse) {
        return Boolean.TRUE.equals(MapUtils.getBoolean(supplementalInfoResponse, "CHANGE_METHOD"));
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
