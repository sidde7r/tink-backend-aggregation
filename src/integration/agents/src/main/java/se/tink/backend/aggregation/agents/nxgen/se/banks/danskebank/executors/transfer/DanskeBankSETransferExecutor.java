package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.transfer;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.TransferConfig;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ListPayeesRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Transfer;

public class DanskeBankSETransferExecutor implements BankTransferExecutor {

    private final DanskeBankSEApiClient apiClient;
    private final DanskeBankSEConfiguration configuration;
    private final DanskeBankExecutorHelper executorHelper;
    private final TransferMessageFormatter transferMessageFormatter;

    private static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG =
            TransferMessageLengthConfig.createWithMaxLength(
                    TransferConfig.SOURCE_MESSAGE_MAX_LENGTH,
                    TransferConfig.DESTINATION_MESSAGE_MAX_LENGTH);

    public DanskeBankSETransferExecutor(
            DanskeBankSEApiClient apiClient,
            DanskeBankConfiguration configuration,
            DanskeBankExecutorHelper executorHelper,
            Catalog catalog) {
        this.apiClient = apiClient;
        this.configuration = (DanskeBankSEConfiguration) configuration;
        this.executorHelper = executorHelper;
        transferMessageFormatter =
                new TransferMessageFormatter(
                        catalog,
                        TRANSFER_MESSAGE_LENGTH_CONFIG,
                        new StringNormalizerSwedish(TransferConfig.WHITE_LISTED_CHARACTER_STRING));
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                transfer.getRemittanceInformation(), RemittanceInformationType.UNSTRUCTURED);

        ListAccountsResponse accounts =
                apiClient.listAccounts(
                        ListAccountsRequest.createFromLanguageCode(
                                configuration.getLanguageCode()));

        boolean isInternalTransfer =
                accounts.isInternalTransfer(transfer.getDestination().getIdentifier());

        return isInternalTransfer
                ? executeInternalTransfer(transfer, accounts, isInternalTransfer)
                : executeExternalTransfer(transfer, accounts, isInternalTransfer);
    }

    private Optional<String> executeInternalTransfer(
            Transfer transfer, ListAccountsResponse accounts, boolean isOwnAccount) {
        Date paymentDate = executorHelper.validatePaymentDate(transfer, isOwnAccount);

        AccountEntity ownDestinationAccount =
                accounts.findAccount(transfer.getDestination().getIdentifier());

        RegisterPaymentResponse registerPaymentResponse =
                executorHelper.registerInternalTransfer(
                        transfer,
                        accounts,
                        ownDestinationAccount,
                        paymentDate,
                        isOwnAccount,
                        transferMessageFormatter);

        apiClient.acceptSignature(
                executorHelper.getTransferType(isOwnAccount),
                registerPaymentResponse.getSignatureId(),
                null);

        return Optional.empty();
    }

    private Optional<String> executeExternalTransfer(
            Transfer transfer, ListAccountsResponse accounts, boolean isInternalTransfer) {
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
        Date paymentDate = executorHelper.validatePaymentDate(transfer, isInternalTransfer);

        HttpResponse injectJsCheckStep = this.apiClient.collectDynamicChallengeJavascript();

        RegisterPaymentResponse registerPaymentResponse =
                executorHelper.registerExternalTransfer(
                        transfer,
                        accounts,
                        creditorName.getCreditorName(),
                        creditorBankName.getBankName(),
                        paymentDate,
                        isInternalTransfer,
                        transferMessageFormatter);

        executorHelper.signPayment(registerPaymentResponse);

        apiClient.acceptSignature(
                executorHelper.getTransferType(isInternalTransfer),
                registerPaymentResponse.getSignatureId(),
                executorHelper.getSignaturePackage(
                        injectJsCheckStep,
                        registerPaymentResponse.getUserId(),
                        registerPaymentResponse.getSignatureText()));

        return Optional.empty();
    }
}
