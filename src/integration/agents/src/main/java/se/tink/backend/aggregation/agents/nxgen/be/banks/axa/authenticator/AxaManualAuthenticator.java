package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import java.util.Arrays;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveTypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public final class AxaManualAuthenticator implements ProgressiveTypedAuthenticator {

    private final AxaApiClient apiClient;
    private final AxaStorage storage;
    private final SupplementalInformationFormer supplementalInformationFormer;

    public AxaManualAuthenticator(
            final AxaApiClient apiClient,
            final AxaStorage storage,
            final SupplementalInformationFormer supplementalInformationFormer) {
        this.apiClient = apiClient;
        this.storage = storage;
        this.supplementalInformationFormer = supplementalInformationFormer;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public Iterable<? extends AuthenticationStep> authenticationSteps(
            final Credentials credentials) {
        return Arrays.asList(
                new LoginStep(apiClient, storage, supplementalInformationFormer),
                new FinalStep(apiClient, storage));
    }
}
