package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sebcard.sas;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsConstants;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SasAgent extends SebBrandedCardsAgent {

    public SasAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, SebBrandedCardsConstants.BrandedCardsDenmark.SAS);
    }
}
