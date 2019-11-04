package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntityImpl;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment.HandelsbankenSEPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class HandelsbankenSEAgent extends HandelsbankenBaseAgent
        implements RefreshTransferDestinationExecutor {

    private final HandelsbankenAccountConverter accountConverter;

    public HandelsbankenSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.accountConverter = new HandelsbankenAccountConverter();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected HandelsbankenBaseAccountConverter getAccountConverter() {
        return accountConverter;
    }

    @Override
    protected Date setMaxPeriodTransactions() {
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -HandelsbankenSEConstants.MAX_FETCH_PERIOD_MONTHS);
        sessionStorage.put(HandelsbankenBaseConstants.StorageKeys.MAX_FETCH_PERIOD_MONTHS, date);

        return date;
    }

    @Override
    protected String getMarket() {
        return Market.SWEDEN;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdAuthenticationController bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        supplementalRequester,
                        new HandelsbankenBankIdAuthenticator(apiClient, sessionStorage),
                        persistentStorage,
                        credentials);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                bankIdAuthenticationController,
                bankIdAuthenticationController);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final HandelsbankenSEPaymentExecutor paymentExecutor =
                new HandelsbankenSEPaymentExecutor(
                        apiClient, credentials, supplementalRequester, persistentStorage);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return new FetchTransferDestinationsResponse(
                new TransferDestinationPatternBuilder()
                        .setTinkAccounts(accounts)
                        .setSourceAccounts(
                                accounts.stream()
                                        .map(GeneralAccountEntityImpl::createFromCoreAccount)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList()))
                        .setDestinationAccounts(new ArrayList<>())
                        .addMultiMatchPattern(
                                AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                        .addMultiMatchPattern(
                                AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                        .addMultiMatchPattern(
                                AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL)
                        .matchDestinationAccountsOn(
                                AccountIdentifier.Type.SE_SHB_INTERNAL,
                                SwedishSHBInternalIdentifier.class)
                        .build());
    }
}
