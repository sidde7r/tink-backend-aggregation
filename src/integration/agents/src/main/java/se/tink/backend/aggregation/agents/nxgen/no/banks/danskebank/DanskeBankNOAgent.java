package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator.DanskeBankNOBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.mapper.NoAccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.selenium.WebDriverHelper;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, LOANS, MORTGAGE_AGGREGATION})
public final class DanskeBankNOAgent extends DanskeBankAgent {
    public DanskeBankNOAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new DanskeBankNOConfiguration(),
                new NoAccountEntityMapper());
    }

    @Override
    protected DanskeBankApiClient createApiClient(
            TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankNOApiClient(
                client, (DanskeBankNOConfiguration) configuration, credentials, catalog);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankNOBankIdAuthenticator danskeBankNoBankIdAuthenticator =
                new DanskeBankNOBankIdAuthenticator(
                        (DanskeBankNOApiClient) apiClient,
                        persistentStorage,
                        credentials,
                        deviceId,
                        configuration,
                        new WebDriverHelper());
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                danskeBankNoBankIdAuthenticator,
                danskeBankNoBankIdAuthenticator);
    }
    //    Investments are temporarly disabled for Norwegian Agents ITE-1676,
    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return new FetchInvestmentAccountsResponse(Collections.emptyMap());
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }
}
