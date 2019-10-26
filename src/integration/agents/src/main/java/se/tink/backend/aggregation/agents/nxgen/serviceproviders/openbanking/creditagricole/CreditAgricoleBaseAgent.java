package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import se.tink.backend.aggregation.agents.*;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.CreditAgricoleBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseClientConfigurationService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.CreditAgricoleBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditAgricoleBaseAgent extends NextGenerationAgent
    implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

  private final String clientName;
  private final CreditAgricoleBaseApiClient apiClient;
  private final TransactionalAccountRefreshController transactionalAccountRefreshController;
  private CreditAgricoleBaseConfiguration creditAgricoleConfiguration;

  public CreditAgricoleBaseAgent(CredentialsRequest request,
                                 AgentContext context,
                                 SignatureKeyPair signatureKeyPair,
                                 BankEnum bank) {
    super(request, context, signatureKeyPair, true);

    persistentStorage.put(CreditAgricoleBaseConstants.StorageKeys.BANK_ENUM, bank);

    this.clientName = request.getProvider().getPayload();
    this.apiClient = new CreditAgricoleBaseApiClient(client, persistentStorage);
    this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
  }

  @Override
  public void setConfiguration(AgentsServiceConfiguration configuration) {
    super.setConfiguration(configuration);
    creditAgricoleConfiguration = CreditAgricoleBaseClientConfigurationService.getInstance()
        .getConfiguration(configuration, clientName, apiClient, client,
            context, this.getAgentClass(), getAgentConfigurationController());
  }

  @Override
  public FetchAccountsResponse fetchCheckingAccounts() {
    return transactionalAccountRefreshController.fetchCheckingAccounts();
  }

  @Override
  public FetchTransactionsResponse fetchCheckingTransactions() {
    return transactionalAccountRefreshController.fetchCheckingTransactions();
  }

  @Override
  public FetchAccountsResponse fetchSavingsAccounts() {
    return transactionalAccountRefreshController.fetchSavingsAccounts();
  }

  @Override
  public FetchTransactionsResponse fetchSavingsTransactions() {
    return transactionalAccountRefreshController.fetchSavingsTransactions();
  }

  @Override
  protected Authenticator constructAuthenticator() {
    final OAuth2AuthenticationController controller =
        new OAuth2AuthenticationController(
            persistentStorage,
            supplementalInformationHelper,
            new CreditAgricoleBaseAuthenticator(
                apiClient, persistentStorage, creditAgricoleConfiguration),
            credentials,
            strongAuthenticationState);

    return new AutoAuthenticationController(
        request,
        systemUpdater,
        new ThirdPartyAppAuthenticationController<>(
            controller, supplementalInformationHelper),
        controller);
  }

  @Override
  protected SessionHandler constructSessionHandler() {
    return SessionHandler.alwaysFail();
  }

  private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
    final CreditAgricoleBaseTransactionalAccountFetcher accountFetcher =
        new CreditAgricoleBaseTransactionalAccountFetcher(apiClient);

    return new TransactionalAccountRefreshController(
        metricRefreshController, updateController, accountFetcher, accountFetcher);
  }

}
