package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sebkort.quintessentially;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsConstants;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class QuintessentiallyTheCreditCardDkAgent extends SebBrandedCardsAgent {

    public QuintessentiallyTheCreditCardDkAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                SebBrandedCardsConstants.BrandedCards.Denmark.QUINTESSENTIALLY);
    }
}
