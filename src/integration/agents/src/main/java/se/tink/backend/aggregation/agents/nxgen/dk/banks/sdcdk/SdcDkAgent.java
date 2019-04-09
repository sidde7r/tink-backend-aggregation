package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk.parser.SdcDkTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcPinAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcSmsOtpAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcTransactionFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

/*
 * Configure market specific client, this is DK
 */
public class SdcDkAgent extends SdcAgent {
    private static Logger LOG = LoggerFactory.getLogger(SdcDkAgent.class);
    private static final int DK_MAX_CONSECUTIVE_EMPTY_PAGES = 8;

    public SdcDkAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new SdcDkConfiguration(request.getProvider()),
                new SdcDkTransactionParser());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        if (SdcDkConstants.Authentication.BANKS_WITH_SMS_AUTHENTICATION.contains(
                agentConfiguration.getBankCode())) {
            return constructSmsAuthenticator();
        } else {
            return constructPinAuthenticator();
        }
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new SdcAccountFetcher(
                                this.bankClient, this.sdcSessionStorage, this.agentConfiguration),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new SdcTransactionFetcher(
                                                this.bankClient,
                                                this.sdcSessionStorage,
                                                this.parser),
                                        DK_MAX_CONSECUTIVE_EMPTY_PAGES))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        SdcCreditCardFetcher creditCardFetcher =
                new SdcCreditCardFetcher(
                        this.bankClient,
                        this.sdcSessionStorage,
                        this.parser,
                        this.agentConfiguration);

        return Optional.of(
                new CreditCardRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        creditCardFetcher, DK_MAX_CONSECUTIVE_EMPTY_PAGES))));
    }

    private Authenticator constructSmsAuthenticator() {
        LOG.info("SDC bank using SMS authentication");
        SdcAutoAuthenticator dkAutoAuthenticator =
                new SdcAutoAuthenticator(
                        bankClient,
                        sdcSessionStorage,
                        agentConfiguration,
                        credentials,
                        sdcPersistentStorage);
        SdcSmsOtpAuthenticator dkSmsOtpAuthenticator =
                new SdcSmsOtpAuthenticator(
                        bankClient,
                        sdcSessionStorage,
                        agentConfiguration,
                        credentials,
                        sdcPersistentStorage);

        SmsOtpAuthenticationPasswordController smsOtpController =
                new SmsOtpAuthenticationPasswordController(
                        catalog, supplementalInformationHelper, dkSmsOtpAuthenticator);

        return new AutoAuthenticationController(
                request, systemUpdater, smsOtpController, dkAutoAuthenticator);
    }

    private Authenticator constructPinAuthenticator() {
        LOG.info("SDC bank using pin authentication");

        SdcPinAuthenticator dkAuthenticator =
                new SdcPinAuthenticator(bankClient, sdcSessionStorage, agentConfiguration);

        return new PasswordAuthenticationController(dkAuthenticator);
    }

    @Override
    protected SdcApiClient createApiClient(SdcConfiguration agentConfiguration) {
        return new SdcApiClient(client, agentConfiguration);
    }
}
