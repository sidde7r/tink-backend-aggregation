package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.CreditMutuelPfmCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.CreditMutuelPfmCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm.EuroInformationNoPfmCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm.EuroInformationNoPfmCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class CreditMutuelAgent extends EuroInformationAgent {

    public CreditMutuelAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new CreditMutuelConfiguration(), new CreditMutuelApiClientFactory());
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        if (this.sessionStorage.get(EuroInformationConstants.Tags.PFM_ENABLED, Boolean.class).orElse(false)) {
            return Optional.of(new CreditCardRefreshController(
                    metricRefreshController,
                    updateController,
                    CreditMutuelPfmCreditCardFetcher.create(this.apiClient),
                    CreditMutuelPfmCreditCardTransactionsFetcher.create(this.apiClient)
            ));
        }
        return Optional.of(new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                EuroInformationNoPfmCreditCardFetcher.create(this.apiClient, this.sessionStorage),
                EuroInformationNoPfmCreditCardTransactionsFetcher.create(this.apiClient)
        ));
    }
}
