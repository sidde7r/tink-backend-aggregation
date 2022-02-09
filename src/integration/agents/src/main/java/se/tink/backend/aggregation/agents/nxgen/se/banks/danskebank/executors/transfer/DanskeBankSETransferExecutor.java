package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.transfer;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.TransferAccountType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.TransferConfig;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.TransferPayType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.DanskeBankExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.AcceptSignatureRequest;
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
import se.tink.libraries.i18n_aggregation.Catalog;
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
                transfer.getRemittanceInformation(), null, RemittanceInformationType.UNSTRUCTURED);

        ListAccountsResponse accounts =
                apiClient.listAccounts(
                        ListAccountsRequest.createFromLanguageCode(
                                configuration.getLanguageCode()));

        boolean isOwnDestinationAccount =
                accounts.isOwnAccount(transfer.getDestination().getIdentifier());

        return isOwnDestinationAccount
                ? executeOwnDestinationAccountTransfer(transfer, accounts, isOwnDestinationAccount)
                : executeExternalTransfer(transfer, accounts, isOwnDestinationAccount);
    }

    private Optional<String> executeOwnDestinationAccountTransfer(
            Transfer transfer, ListAccountsResponse accounts, boolean isOwnDestinationAccount) {
        Date paymentDate =
                executorHelper.validatePaymentDate(
                        transfer, TransferAccountType.INTERNAL, TransferPayType.ACCOUNT);

        AccountEntity ownDestinationAccount =
                accounts.findAccount(transfer.getDestination().getIdentifier());

        RegisterPaymentResponse registerPaymentResponse =
                executorHelper
                        .registerOwnDestinationAccountTransfer(
                                transfer,
                                accounts,
                                ownDestinationAccount,
                                paymentDate,
                                isOwnDestinationAccount,
                                transferMessageFormatter)
                        .validate();

        apiClient
                .acceptSignature(
                        executorHelper.getTransferType(isOwnDestinationAccount),
                        AcceptSignatureRequest.builder()
                                .signatureId(registerPaymentResponse.getSignatureId())
                                .signaturePackage(null)
                                .language(configuration.getLanguageCode())
                                .build())
                .validate();

        return Optional.empty();
    }

    private Optional<String> executeExternalTransfer(
            Transfer transfer, ListAccountsResponse accounts, boolean isOwnDestinationAccount) {
        apiClient.getBeneficiaries(
                ListPayeesRequest.builder().languageCode(configuration.getLanguageCode()).build());

        CreditorRequest creditorRequest =
                CreditorRequest.builder()
                        .accountNumber(transfer.getDestination().getIdentifier())
                        .countryCode(configuration.getMarketCode())
                        .build();
        CreditorResponse creditorName = apiClient.creditorName(creditorRequest);
        CreditorResponse creditorBankName = apiClient.creditorBankName(creditorRequest);

        Date paymentDate =
                executorHelper.validatePaymentDate(
                        transfer, TransferAccountType.EXTERNAL, TransferPayType.ACCOUNT);

        HttpResponse injectJsCheckStep = this.apiClient.collectDynamicChallengeJavascript();

        RegisterPaymentResponse registerPaymentResponse =
                executorHelper
                        .registerExternalTransfer(
                                transfer,
                                accounts,
                                creditorName.getCreditorName(),
                                creditorBankName.getBankName(),
                                paymentDate,
                                isOwnDestinationAccount,
                                transferMessageFormatter)
                        .validate();

        executorHelper.signPayment(registerPaymentResponse);

        apiClient
                .acceptSignature(
                        executorHelper.getTransferType(isOwnDestinationAccount),
                        AcceptSignatureRequest.builder()
                                .signatureId(registerPaymentResponse.getSignatureId())
                                .signaturePackage(
                                        executorHelper.getSignaturePackage(
                                                injectJsCheckStep,
                                                registerPaymentResponse.getUserId(),
                                                registerPaymentResponse.getSignatureText()))
                                .language(configuration.getLanguageCode())
                                .build())
                .validate();

        return Optional.empty();
    }
}
