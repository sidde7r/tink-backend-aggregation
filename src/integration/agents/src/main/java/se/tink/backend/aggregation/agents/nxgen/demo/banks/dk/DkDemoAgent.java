package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoCreditCardAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoIdentityData;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n_aggregation.Catalog;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class DkDemoAgent extends NextGenerationDemoAgent {

    private final Provider provider;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    @Inject
    public DkDemoAgent(AgentComponentProvider componentProvider, CredentialsRequest request) {
        super(componentProvider);
        this.provider = request.getProvider();
        this.catalog = componentProvider.getContext().getCatalog();
        this.supplementalInformationController =
                componentProvider.getSupplementalInformationController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DkDemoNemIdAuthenticator nemIdAuthenticator =
                new DkDemoNemIdAuthenticator(catalog, supplementalInformationController);
        DkDemoMitIdAuthenticator mitIdAuthenticator =
                new DkDemoMitIdAuthenticator(supplementalInformationController);

        return new DkDemoAuthenticator(
                provider, persistentStorage, nemIdAuthenticator, mitIdAuthenticator);
    }

    @Override
    public DemoInvestmentAccount getInvestmentAccounts() {
        return null;
    }

    @Override
    public DemoSavingsAccount getDemoSavingsAccounts() {
        return null;
    }

    @Override
    public DemoLoanAccount getDemoLoanAccounts() {
        return null;
    }

    @Override
    public List<DemoTransactionAccount> getTransactionAccounts() {
        return Collections.singletonList(
                new DemoTransactionAccount() {
                    @Override
                    public String getAccountId() {
                        return "8888-111111111111";
                    }

                    @Override
                    public String getAccountName() {
                        return "Debt Account";
                    }

                    @Override
                    public double getBalance() {
                        return 26245.33;
                    }

                    @Override
                    public List<AccountIdentifier> getIdentifiers() {
                        return Collections.emptyList();
                    }
                });
    }

    @Override
    public List<DemoCreditCardAccount> getCreditCardAccounts() {
        return Collections.emptyList();
    }

    @Override
    public DemoIdentityData getIdentityDataResponse() {
        return null;
    }

    @Override
    public SessionHandler constructSessionHandler() {
        return new DkDemoSessionHandler(persistentStorage);
    }
}
