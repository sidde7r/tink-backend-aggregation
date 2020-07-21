package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.steps;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.SamlAuthnResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class SmsOtpStep extends AbstractAuthenticationStep {
    private final CaisseEpargneApiClient apiClient;
    private final Storage instanceStorage;
    private final SupplementalInformationProvider supplementalInformationProvider;
    public static final String STEP_ID = "smsOtpStep";

    public SmsOtpStep(
            CaisseEpargneApiClient apiClient,
            Storage instanceStorage,
            SupplementalInformationProvider supplementalInformationProvider) {
        super(STEP_ID);
        this.apiClient = apiClient;
        this.instanceStorage = instanceStorage;
        this.supplementalInformationProvider = supplementalInformationProvider;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        SamlAuthnResponse credentialsResponse =
                instanceStorage
                        .get(StorageKeys.CREDENTIALS_RESPONSE, SamlAuthnResponse.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Credentials response missing from storage."));
        String samlTransactionPath = instanceStorage.get(StorageKeys.SAML_TRANSACTION_PATH);

        String otp =
                supplementalInformationProvider
                        .getSupplementalInformationHelper()
                        .waitForOtpInput();
        String validationId =
                credentialsResponse
                        .getValidationId()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Not able to determine validation id."));
        String validationUnitId =
                credentialsResponse
                        .getValidationUnitId()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Not able to determine validation unit id."));
        SamlAuthnResponse otpResponse =
                apiClient.submitOtp(validationId, validationUnitId, otp, samlTransactionPath);
        otpResponse.throwIfFailedAuthentication();
        instanceStorage.put(StorageKeys.CREDENTIALS_RESPONSE, otpResponse);
        return AuthenticationStepResponse.executeNextStep();
    }
}
