package se.tink.backend.aggregation.nxgen.agents;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class NextGenerationAgent extends SubsequentGenerationAgent {

    protected final SupplementalInformationHelper supplementalInformationHelper;
    protected final SupplementalInformationController supplementalInformationController;

    protected NextGenerationAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.supplementalInformationController =
                new SupplementalInformationController(supplementalRequester, credentials);
        this.supplementalInformationHelper =
                new SupplementalInformationHelper(
                        request.getProvider(), supplementalInformationController);
    }
}
