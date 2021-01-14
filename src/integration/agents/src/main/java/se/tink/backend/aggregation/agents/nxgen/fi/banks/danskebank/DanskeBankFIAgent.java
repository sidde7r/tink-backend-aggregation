package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank.rpc.FetchHouseholdFIResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankChallengeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.FiIdentityData;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, LOANS})
public final class DanskeBankFIAgent extends DanskeBankAgent
        implements RefreshIdentityDataExecutor {
    @Inject
    public DanskeBankFIAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new AccountEntityMapper(MarketCode.FI.name()));
    }

    @Override
    protected DanskeBankConfiguration createConfiguration() {
        return new DanskeBankFIConfiguration();
    }

    @Override
    protected DanskeBankApiClient createApiClient(
            TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankFIApiClient(
                client, (DanskeBankFIConfiguration) configuration, credentials, catalog);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankChallengeAuthenticator danskeBankChallengeAuthenticator =
                new DanskeBankChallengeAuthenticator(
                        catalog,
                        supplementalInformationHelper,
                        supplementalRequester,
                        apiClient,
                        client,
                        persistentStorage,
                        credentials,
                        deviceId,
                        configuration);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                danskeBankChallengeAuthenticator,
                danskeBankChallengeAuthenticator);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final DanskeBankFIApiClient fiApiClient = (DanskeBankFIApiClient) apiClient;
        final FetchHouseholdFIResponse response = fiApiClient.fetchHousehold();
        final String customerName = response.getCustomerName();
        final boolean missingName = Strings.isNullOrEmpty(customerName);
        final boolean missingSsn = Strings.isNullOrEmpty(response.getCustomerExternalId());

        if (missingName && missingSsn) {
            throw new NoSuchElementException("Missing customer name and external ID.");
        }

        IdentityData identityData;
        try {
            identityData = FiIdentityData.of(customerName, response.getCustomerExternalId());
        } catch (IllegalArgumentException e) {
            // null or invalid SSN
            if (missingName) {
                throw new NoSuchElementException("Missing customer name, invalid external ID.");
            }
            identityData =
                    IdentityData.builder().setFullName(customerName).setDateOfBirth(null).build();
        }

        return new FetchIdentityDataResponse(identityData);
    }
}
