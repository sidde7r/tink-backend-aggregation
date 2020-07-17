package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.steps;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.SamlAuthnResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class FinalizeAuthStep extends AbstractAuthenticationStep {
    public static final String STEP_ID = "finalizeAuthStep";
    private final CaisseEpargneApiClient apiClient;
    private final Storage instanceStorage;

    public FinalizeAuthStep(CaisseEpargneApiClient apiClient, Storage instanceStorage) {
        super(STEP_ID);
        this.apiClient = apiClient;
        this.instanceStorage = instanceStorage;
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
                                                "SAML Authn response missing from storage."));
        apiClient.oAuth2Consume(
                credentialsResponse
                        .getSaml2PostAction()
                        .orElseThrow(
                                () -> new IllegalStateException("SAML action URL is missing.")),
                credentialsResponse
                        .getSamlResponseValue()
                        .orElseThrow(() -> new IllegalStateException("SAML response missing.")));

        String bankId = instanceStorage.get(StorageKeys.BANK_ID);
        apiClient.soapActionSsoBapi(bankId);
        return AuthenticationStepResponse.authenticationSucceeded();
    }
}
