package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sebkort.sas;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsConstants;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SasEurobonusMastercardNoAgent extends SebBrandedCardsAgent {

    public SasEurobonusMastercardNoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, SebBrandedCardsConstants.BrandedCards.Norway.SAS);
    }
}
