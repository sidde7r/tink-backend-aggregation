package se.tink.backend.aggregation.agents.nxgen.at.openbanking.ing;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngAgent extends IngBaseAgent {

    public IngAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected LocalDate earliestTransactionHistoryDate() {
        return LocalDate.now().minusYears(7);
    }
}
