package se.tink.backend.aggregation.nxgen.agents;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SupplementalInformationProvider {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SupplementalInformationController supplementalInformationController;

    public SupplementalInformationProvider(
            CredentialsRequest request,
            SupplementalRequester supplementalRequester,
            Credentials credentials) {
        this.supplementalInformationController =
                new SupplementalInformationController(supplementalRequester, credentials);
        this.supplementalInformationHelper =
                new SupplementalInformationHelper(
                        request.getProvider(), supplementalInformationController);
    }

    public SupplementalInformationHelper getSupplementalInformationHelper() {
        return supplementalInformationHelper;
    }

    public SupplementalInformationController getSupplementalInformationController() {
        return supplementalInformationController;
    }
}
