package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.HandelsbankenFICardDeviceAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class HandelsbankenFIAgent extends HandelsbankenAgent<HandelsbankenFIApiClient, HandelsbankenFIConfiguration> {
    public HandelsbankenFIAgent(CredentialsRequest request, AgentContext context, String signatureKeyPath) {
        super(request, context, signatureKeyPath, new HandelsbankenFIConfiguration());
    }

    @Override
    protected HandelsbankenFIApiClient constructApiClient(HandelsbankenFIConfiguration handelsbankenConfiguration) {
        return new HandelsbankenFIApiClient(this.client, handelsbankenConfiguration);
    }

    @Override
    protected TypedAuthenticator[] constructAuthenticators(
            HandelsbankenFIApiClient bankClient,
            HandelsbankenFIConfiguration handelsbankenConfiguration,
            HandelsbankenPersistentStorage handelsbankenPersistentStorage,
            HandelsbankenSessionStorage handelsbankenSessionStorage) {
        return new TypedAuthenticator[] {
            constructAutoAuthenticationController(
                    new HandelsbankenFICardDeviceAuthenticator(bankClient, handelsbankenPersistentStorage,
                            new SupplementalInformationController(this.context, this.credentials),
                            handelsbankenConfiguration,
                            new HandelsbankenAutoAuthenticator(bankClient, handelsbankenPersistentStorage,
                                    this.credentials,
                                    handelsbankenSessionStorage, handelsbankenConfiguration)
                    )
            )
        };
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController(HandelsbankenFIApiClient bankClient,
            HandelsbankenSessionStorage handelsbankenSessionStorage) {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferController> constructTranferController(HandelsbankenFIApiClient client,
            HandelsbankenSessionStorage sessionStorage, AgentContext context) {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController(
            HandelsbankenFIApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return Optional.empty();
    }
}
