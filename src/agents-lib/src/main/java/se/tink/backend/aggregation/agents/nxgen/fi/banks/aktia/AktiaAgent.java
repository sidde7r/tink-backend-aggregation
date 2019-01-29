package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.AktiaAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.AktiaEncapConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.AktiaKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.AktiaCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.AktiaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.AktiaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.AktiaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.filters.AktiaHttpFilter;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapClient;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class AktiaAgent extends NextGenerationAgent {
    private final AktiaApiClient apiClient;

    public AktiaAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = AktiaApiClient.createApiClient(client, credentials);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new AktiaHttpFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        EncapClient encapClient = new EncapClient(new AktiaEncapConfiguration(), persistentStorage, client, true,
                credentials.getField(Field.Key.USERNAME));

        return new AutoAuthenticationController(request, context,
                new KeyCardAuthenticationController(catalog, supplementalInformationHelper,
                        AktiaKeyCardAuthenticator.createKeyCardAuthenticator(supplementalInformationHelper, catalog,
                                apiClient, credentials, sessionStorage, encapClient)),
                new AktiaAutoAuthenticator(encapClient, apiClient, credentials));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(metricRefreshController, updateController,
                        AktiaTransactionalAccountFetcher.create(apiClient),
                        AktiaTransactionFetcher.create(apiClient)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(metricRefreshController, updateController,
                        AktiaCreditCardAccountFetcher.create(apiClient),
                        AktiaTransactionFetcher.create(apiClient)
                )
        );
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(metricRefreshController, updateController,
                        AktiaInvestmentFetcher.create(apiClient, credentials)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return AktiaSessionHandler.createFromApiClientAndCredentials(apiClient, credentials);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
