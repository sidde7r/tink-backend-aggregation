package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.transfer;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ListPayeesRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.transfer.rpc.Transfer;

public class DanskeBankSETransferExecutor implements BankTransferExecutor {

    private final DanskeBankSEApiClient apiClient;
    private final DanskeBankSEConfiguration configuration;
    private final DanskeBankExecutorHelper executorHelper;

    public DanskeBankSETransferExecutor(
            DanskeBankSEApiClient apiClient,
            DanskeBankConfiguration configuration,
            DanskeBankExecutorHelper executorHelper) {
        this.apiClient = apiClient;
        this.configuration = (DanskeBankSEConfiguration) configuration;
        this.executorHelper = executorHelper;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        ListAccountsResponse accounts =
                apiClient.listAccounts(
                        ListAccountsRequest.createFromLanguageCode(
                                configuration.getLanguageCode()));

        boolean isOwnAccount = accounts.isOwnAccount(transfer.getDestination().getIdentifier());

        return isOwnAccount
                ? executeInternalTransfer(transfer, accounts, isOwnAccount)
                : executeExternalTransfer(transfer, accounts, isOwnAccount);
    }

    private Optional executeInternalTransfer(
            Transfer transfer, ListAccountsResponse accounts, boolean isOwnAccount) {
        Date paymentDate = executorHelper.validatePaymentDate(transfer, isOwnAccount);

        AccountEntity ownDestinationAccount =
                accounts.findAccount(transfer.getDestination().getIdentifier());

        RegisterPaymentResponse registerPaymentResponse =
                executorHelper.registerInternalTransfer(
                        transfer, accounts, ownDestinationAccount, paymentDate, isOwnAccount);

        apiClient.acceptSignature(
                executorHelper.getTransferType(isOwnAccount),
                registerPaymentResponse.getSignatureId(),
                null);

        return Optional.empty();
    }

    private Optional executeExternalTransfer(
            Transfer transfer,
            ListAccountsResponse accounts,
            boolean isInternalDestinationAccount) {
        apiClient.listPayees(ListPayeesRequest.create(configuration.getLanguageCode()));

        CreditorResponse creditorName =
                apiClient.creditorName(
                        CreditorRequest.create(
                                transfer.getDestination().getIdentifier(),
                                configuration.getMarketCode()));

        CreditorResponse creditorBankName =
                apiClient.creditorBankName(
                        CreditorRequest.create(
                                transfer.getDestination().getIdentifier(),
                                configuration.getMarketCode()));
        Date paymentDate =
                executorHelper.validatePaymentDate(transfer, isInternalDestinationAccount);

        HttpResponse injectJsCheckStep = this.apiClient.collectDynamicChallengeJavascript();

        RegisterPaymentResponse registerPaymentResponse =
                executorHelper.registerExternalTransfer(
                        transfer,
                        accounts,
                        creditorName.getCreditorName(),
                        creditorBankName.getBankName(),
                        paymentDate,
                        isInternalDestinationAccount);

        executorHelper.signPayment(registerPaymentResponse);

        apiClient.acceptSignature(
                executorHelper.getTransferType(isInternalDestinationAccount),
                registerPaymentResponse.getSignatureId(),
                executorHelper.getSignaturePackage(
                        injectJsCheckStep,
                        registerPaymentResponse.getUserId(),
                        registerPaymentResponse.getSignatureText()));

        return Optional.empty();
    }
}
