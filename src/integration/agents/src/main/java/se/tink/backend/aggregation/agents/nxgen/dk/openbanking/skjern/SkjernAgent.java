package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.skjern;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SkjernAgent extends BankdataAgent {

    public SkjernAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                SkjernConstants.BASE_URL,
                SkjernConstants.BASE_AUTH_URL);
    }
}
