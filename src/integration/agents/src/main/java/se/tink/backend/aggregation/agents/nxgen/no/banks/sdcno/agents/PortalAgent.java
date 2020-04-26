package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.agents;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.CreditCardTransactionFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class PortalAgent extends SdcNoBaseAgent implements RefreshCreditCardAccountsExecutor {

    private final CreditCardRefreshController creditCardRefreshController;

    public PortalAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        creditCardRefreshController = constructCreditCardRefreshController();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                new CreditCardFetcher(bankClient),
                new CreditCardTransactionFetcher(bankClient));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }
}
