package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebkort.quintessentially;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsConstants;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class QuintessentiallyTheCreditCardSeAgent extends SebBrandedCardsAgent {

    public QuintessentiallyTheCreditCardSeAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                SebBrandedCardsConstants.BrandedCards.Sweden.QUINTESSENTIALLY);
    }
}
