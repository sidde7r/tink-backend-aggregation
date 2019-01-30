package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.authenticator.BnpPfAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.fetcher.BnpPfTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.filter.BnpPfHttpFilter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class BnpPfAgent extends NextGenerationAgent {

    private static final String BNPPF_CERT_PATH = "/etc/tink/bnppf-cert.p12";

    private final BnpPfApiClient apiClient;

    public BnpPfAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new BnpPfApiClient(client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        try {
            byte[] clientCertificateBytes = FileUtils.readFileToByteArray(new File(BNPPF_CERT_PATH));
            client.setSslClientCertificate(clientCertificateBytes, "");
            client.addFilter(new BnpPfHttpFilter(credentials.getField(Field.Key.ACCESS_TOKEN)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BnpPfAuthenticator();
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        BnpPfTransactionalAccountFetcher transactionalAccountFetcher =
                new BnpPfTransactionalAccountFetcher(apiClient);
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                transactionalAccountFetcher, transactionalAccountFetcher));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
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
        return new BnpPfSessionHandler();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
