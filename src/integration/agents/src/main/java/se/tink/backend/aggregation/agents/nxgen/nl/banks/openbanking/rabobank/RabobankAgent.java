package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.RabobankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.session.RabobankSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils.RabobankUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

import java.util.Base64;
import java.util.Optional;

public class RabobankAgent extends NextGenerationAgent {

    private final RabobankApiClient apiClient;
    private final String clientName;

    public RabobankAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new RabobankApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final RabobankConfiguration rabobankConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(RabobankConstants.INTEGRATION_NAME, clientName, RabobankConfiguration.class)
                        .orElseThrow(
                                () -> new IllegalStateException("Rabobank configuration missing."));
        if (!rabobankConfiguration.isValid()) {
            throw new IllegalStateException("Rabobank configuration is invalid.");
        }

        final String clientSSLP12 = rabobankConfiguration.getClientSSLP12();
        final String password = rabobankConfiguration.getClientSSLKeyPassword();
        final byte[] p12 = Base64.getDecoder().decode(clientSSLP12);
        client.setSslClientCertificate(p12, password);

        persistentStorage.put(
                RabobankConstants.StorageKey.CLIENT_ID, rabobankConfiguration.getClientId());
        persistentStorage.put(
                RabobankConstants.StorageKey.CLIENT_SECRET,
                rabobankConfiguration.getClientSecret());
        persistentStorage.put(
                RabobankConstants.StorageKey.REDIRECT_URL, rabobankConfiguration.getRedirectUrl());
        persistentStorage.put(RabobankConstants.StorageKey.CLIENT_CERT_KEY_PASSWORD, password);
        persistentStorage.put(RabobankConstants.StorageKey.CLIENT_SSL_P12, clientSSLP12);
        persistentStorage.put(
                RabobankConstants.StorageKey.CLIENT_CERT,
                RabobankUtils.getB64EncodedX509Certificate(p12, password));
        persistentStorage.put(
                RabobankConstants.StorageKey.CLIENT_CERT_SERIAL,
                RabobankUtils.getCertificateSerialNumber(p12, password));
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        final RabobankAuthenticator authenticator =
                new RabobankAuthenticator(apiClient, persistentStorage);

        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage, supplementalInformationHelper, authenticator);

        final ThirdPartyAppAuthenticationController<String> thirdParty =
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper);

        return new AutoAuthenticationController(request, context, thirdParty, controller);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new TransactionalAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        new TransactionFetcher(apiClient),
                                        RabobankConstants.START_PAGE))));
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
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new RabobankSessionHandler();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
