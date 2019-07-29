package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebcard.nordicchoiceclub;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsConstants;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordicChoiceClubSEAgent extends SebBrandedCardsAgent {

    public NordicChoiceClubSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                SebBrandedCardsConstants.BrandedCardsSweden.NORDIC_CHOICE_CLUB);
    }
}
