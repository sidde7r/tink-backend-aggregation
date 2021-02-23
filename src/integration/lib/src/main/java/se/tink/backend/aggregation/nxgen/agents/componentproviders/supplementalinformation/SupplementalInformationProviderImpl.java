package se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationControllerImpl;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelperImpl;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SupplementalInformationProviderImpl implements SupplementalInformationProvider {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SupplementalInformationController supplementalInformationController;

    /**
     * Do not construct your own SupplementalInformationProviderImpl. Use the instances of
     * SupplementalInformationController and SupplementalInformationHelper available to your agent
     * from SubsequentGenerationAgent instead. Or even better, migrate to AgentPlatformAgent or
     * SubsequentProgressiveGenerationAgent where the Supplemental information controlling is
     * outside of the agent and you do not need to have an instance.
     */
    @Inject
    public SupplementalInformationProviderImpl(
            final SupplementalRequester supplementalRequester,
            final CredentialsRequest credentialsRequest) {

        this.supplementalInformationController =
                new SupplementalInformationControllerImpl(
                        supplementalRequester,
                        credentialsRequest.getCredentials(),
                        credentialsRequest.getState(),
                        credentialsRequest.getProvider().getClassName());
        this.supplementalInformationHelper =
                new SupplementalInformationHelperImpl(
                        credentialsRequest.getProvider(), supplementalInformationController);
    }

    @Override
    public SupplementalInformationHelper getSupplementalInformationHelper() {
        return supplementalInformationHelper;
    }

    @Override
    public SupplementalInformationController getSupplementalInformationController() {
        return supplementalInformationController;
    }
}
