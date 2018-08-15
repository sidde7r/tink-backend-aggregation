package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.HandelsbankenSECardDeviceAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.HandelsbankenSEEInvoiceExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.HandelsbankenSEPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.HandelsbankenSEBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.einvoice.HandelsbankenSEEInvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.HandelsbankenSEInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transferdestination.HandelsbankenSETransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.backend.common.config.SignatureKeyPair;
import se.tink.libraries.i18n.Catalog;

public class HandelsbankenSEAgent extends HandelsbankenAgent<HandelsbankenSEApiClient, HandelsbankenSEConfiguration> {

    public HandelsbankenSEAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new HandelsbankenSEConfiguration());
    }

    @Override
    protected HandelsbankenSEApiClient constructApiClient(HandelsbankenSEConfiguration handelsbankenConfiguration) {
        return new HandelsbankenSEApiClient(client, handelsbankenConfiguration);
    }

    @Override
    protected TypedAuthenticator[] constructAuthenticators(
            HandelsbankenSEApiClient bankClient,
            HandelsbankenSEConfiguration handelsbankenConfiguration,
            HandelsbankenPersistentStorage handelsbankenPersistentStorage,
            HandelsbankenSessionStorage handelsbankenSessionStorage) {

        HandelsbankenAutoAuthenticator autoAuthenticator = constructAutoAuthenticator();

        return new TypedAuthenticator[] {
                constructAutoAuthenticationController(
                        new HandelsbankenSECardDeviceAuthenticator(bankClient, handelsbankenPersistentStorage,
                                new SupplementalInformationController(context, credentials),
                                handelsbankenConfiguration, autoAuthenticator),
                        autoAuthenticator),
                new BankIdAuthenticationController<>(context, new HandelsbankenBankIdAuthenticator(bankClient,
                        credentials, handelsbankenPersistentStorage, handelsbankenSessionStorage))
        };
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController(
            HandelsbankenSEApiClient bankClient,
            HandelsbankenSessionStorage handelsbankenSessionStorage) {
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                        new HandelsbankenSEInvestmentFetcher(bankClient, handelsbankenSessionStorage, credentials)
                )
        );
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController(HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        return Optional.of(new EInvoiceRefreshController(
                metricRefreshController,
                updateController,
                new HandelsbankenSEEInvoiceFetcher(client, sessionStorage)
        ));
    }

    @Override
    protected Optional<TransferController> constructTranferController(HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage, AgentContext context) {

        HandelsbankenSEPaymentExecutor paymentExecutor = new HandelsbankenSEPaymentExecutor(client, sessionStorage);

        Catalog catalog = context.getCatalog();
        return Optional.of(new TransferController(
                paymentExecutor,
                new HandelsbankenSEBankTransferExecutor(
                        client,
                        sessionStorage,
                        new ExecutorExceptionResolver(catalog),
                        new TransferMessageFormatter(
                                catalog,
                                TransferMessageLengthConfig.createWithMaxLength(
                                        14,
                                        12),
                                new StringNormalizerSwedish(",.-?!/+"))),
                new HandelsbankenSEEInvoiceExecutor(client, sessionStorage),
                paymentExecutor));
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController(
            HandelsbankenSEApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return Optional.of(new TransferDestinationRefreshController(metricRefreshController, updateController,
                new HandelsbankenSETransferDestinationFetcher(client,sessionStorage)));
    }
}
