package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import java.util.Arrays;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.libraries.i18n.Catalog;

public final class AxaManualAuthenticator
        implements MultiFactorAuthenticator, ProgressiveAuthenticator {

    private final AxaApiClient apiClient;
    private final Catalog catalog;
    private final AxaStorage storage;
    private final SupplementalInformationFormer supplementalInformationFormer;

    public AxaManualAuthenticator(
            final Catalog catalog,
            final AxaApiClient apiClient,
            final AxaStorage storage,
            final SupplementalInformationFormer supplementalInformationFormer) {
        this.apiClient = apiClient;
        this.catalog = catalog;
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

    @Override
    public void authenticate(Credentials credentials) {
        throw new AssertionError(); // Superseded by ProgressiveAuthenticator
    }
}
