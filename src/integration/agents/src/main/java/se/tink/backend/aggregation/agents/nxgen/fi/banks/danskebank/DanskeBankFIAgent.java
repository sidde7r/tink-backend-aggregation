package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank;

import com.google.common.base.Strings;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank.rpc.FetchHouseholdFIResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankChallengeAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.FiIdentityData;

public class DanskeBankFIAgent extends DanskeBankAgent implements RefreshIdentityDataExecutor {
    public DanskeBankFIAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new DanskeBankFIConfiguration());
        configureHttpClient(client);
    }

    @Override
    protected DanskeBankApiClient createApiClient(
            TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankFIApiClient(client, (DanskeBankFIConfiguration) configuration);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.disableSignatureRequestHeader();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankChallengeAuthenticator danskeBankChallengeAuthenticator =
                new DanskeBankChallengeAuthenticator(
                        catalog,
                        supplementalInformationHelper,
                        apiClient,
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
        final DanskeBankFIApiClient fiApiClient = (DanskeBankFIApiClient)apiClient;
        final FetchHouseholdFIResponse response = fiApiClient.fetchHousehold();
        if (Strings.isNullOrEmpty(response.getCustomerName())) {
            throw new NoSuchElementException("Missing customerName.");
        }
        if (Strings.isNullOrEmpty(response.getCustomerExternalId())) {
            throw new NoSuchElementException("Missing customerExternalId.");
        }

        final IdentityData identityData = FiIdentityData.of(response.getCustomerName(), response.getCustomerExternalId());
        return new FetchIdentityDataResponse(identityData);
    }
}
