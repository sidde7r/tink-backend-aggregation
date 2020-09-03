package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.DanskeBankBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.transfer.DanskeBankExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.transfer.DanskeBankSETransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankPasswordAuthenticator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DanskeBankSEAgent extends DanskeBankAgent {
    public DanskeBankSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new DanskeBankSEConfiguration());
        configureHttpClient(client);
    }

    @Override
    protected DanskeBankApiClient createApiClient(
            TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankSEApiClient(
                client, (DanskeBankSEConfiguration) configuration, credentials);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent("Mobilbank/813854 CFNetwork/808.2.16 Darwin/16.3.0");
        client.setDebugOutput(false);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(
                        supplementalRequester,
                        new DanskeBankBankIdAuthenticator(
                                (DanskeBankSEApiClient) apiClient,
                                deviceId,
                                configuration,
                                credentials),
                        persistentStorage,
                        credentials),
                new PasswordAuthenticationController(
                        new DanskeBankPasswordAuthenticator(
                                apiClient, deviceId, configuration, credentials)));
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        DanskeBankExecutorHelper executorHelper =
                new DanskeBankExecutorHelper(
                        (DanskeBankSEApiClient) apiClient,
                        deviceId,
                        configuration,
                        supplementalRequester);
        DanskeBankSETransferExecutor transferExecutor =
                new DanskeBankSETransferExecutor(
                        (DanskeBankSEApiClient) apiClient, configuration, executorHelper, catalog);

        return Optional.of(new TransferController(null, transferExecutor));
    }
}
