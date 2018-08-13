package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.EuroInformationPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional.EuroInformationAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.EuroInformationCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.EuroInformationCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.EuroInformationInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional.EuroInformationTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.EuroInformationSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class EuroInformationAgent extends NextGenerationAgent {
    protected final EuroInformationApiClient apiClient;

    protected EuroInformationAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            EuroInformationConfiguration config) {
        super(request, context, signatureKeyPair);
        this.apiClient = new EuroInformationApiClient(this.client, sessionStorage, config);
    }

    protected EuroInformationAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            EuroInformationConfiguration config, Class<? extends EuroInformationApiClient> apiClientClass) {
        super(request, context, signatureKeyPair);

        try {
            this.apiClient = apiClientClass
                    .getConstructor(TinkHttpClient.class, SessionStorage.class, EuroInformationConfiguration.class)
                    .newInstance(this.client, sessionStorage, config);
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InstantiationException
                | InvocationTargetException
                e) {
            throw new IllegalArgumentException(String.format(
                    "The submitted apiClient-class %s caused the following error: \n%s",
                    apiClientClass.getName(), e.getMessage())
            );
        }
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                EuroInformationPasswordAuthenticator.create(this.apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        EuroInformationAccountFetcher.create(this.apiClient, this.sessionStorage),
                        EuroInformationTransactionsFetcher.create(this.apiClient)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                EuroInformationCreditCardFetcher.create(apiClient, sessionStorage),
                EuroInformationCreditCardTransactionsFetcher.create()
        ));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                EuroInformationInvestmentAccountFetcher.create(this.apiClient, this.sessionStorage)
        ));
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
        return EuroInformationSessionHandler.create(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
