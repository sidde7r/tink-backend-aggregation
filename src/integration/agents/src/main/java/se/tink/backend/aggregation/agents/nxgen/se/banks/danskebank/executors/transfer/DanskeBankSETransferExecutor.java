package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.transfer;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity.BusinessDataEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ListPayeesRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ListPayeesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ValidatePaymentDateRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ValidatePaymentDateResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class DanskeBankSETransferExecutor implements BankTransferExecutor {

    private final DanskeBankSEApiClient apiClient;
    private final String deviceId;
    private final DanskeBankSEConfiguration configuration;

    private String dynamicBankIdJavascript;

    public DanskeBankSETransferExecutor(
            DanskeBankSEApiClient apiClient,
            String deviceId,
            DanskeBankConfiguration configuration) {
        this.apiClient = apiClient;
        this.deviceId = deviceId;
        this.configuration = (DanskeBankSEConfiguration) configuration;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        ListAccountsResponse accounts =
                apiClient.listAccounts(
                        ListAccountsRequest.createFromLanguageCode(
                                configuration.getLanguageCode()));

        ListPayeesResponse listPayees =
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
        Date paymentDate = validatePaymentDate(transfer, accounts);

        // Signature call (TBD)

        registerPayment(transfer, accounts, creditorName, creditorBankName, paymentDate);

        return Optional.empty();
    }

    private Date validatePaymentDate(Transfer transfer, ListAccountsResponse accounts) {
        String transferType =
                accounts.isInternalAccount(transfer.getDestination().getIdentifier())
                        ? "internal"
                        : "external";

        ValidatePaymentDateRequest paymentDateRequest =
                new ValidatePaymentDateRequest(
                        transfer.getDueDate(),
                        configuration.getMarketCode(),
                        false,
                        "",
                        transfer.getDestination().getIdentifier(),
                        transferType);

        ValidatePaymentDateResponse paymentDateResponse =
                apiClient.validatePaymentDate(paymentDateRequest);

        // Case when we get a dueDate from the user and bank sends another date since the user's
        // date is not valid.
        if (transfer.getDueDate() != null
                && !paymentDateResponse.isTransferDateSameAsBookingDate(transfer.getDueDate())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(EndUserMessage.INVALID_DUEDATE_TOO_SOON_OR_NOT_BUSINESSDAY)
                    .setInternalStatus("InvalidDueDate")
                    .build();
        }

        return paymentDateResponse.getBookingDate();
    }

    private String registerPayment(
            Transfer transfer,
            ListAccountsResponse accounts,
            CreditorResponse creditorName,
            CreditorResponse creditorBankName,
            Date paymentDate) {
        AccountEntity sourceAccount =
                accounts.findSourceAccount(transfer.getSource().getIdentifier());

        String transferType =
                accounts.isInternalAccount(transfer.getDestination().getIdentifier())
                        ? "TransferToOwnAccountSE"
                        : "TransferToOtherAccountSE";

        BusinessDataEntity businessData =
                new BusinessDataEntity()
                        .seteInvoiceMarking(false)
                        .setAccountNameFrom(sourceAccount.getAccountName())
                        .setAccountNameTo(creditorName.getCreditorName())
                        .setAccountNoExtFrom(sourceAccount.getAccountNoExt())
                        .setAccountNoIntFrom(sourceAccount.getAccountNoInt())
                        .setAccountNoToExt(transfer.getDestination().getIdentifier())
                        .setAccountProductFrom(sourceAccount.getAccountProduct())
                        .setAllowDuplicateTransfer(true)
                        .setAmount(transfer.getAmount().getValue())
                        .setBankName(creditorBankName.getBankName())
                        .setBookingDate(formatDate(paymentDate))
                        .setCurrency(transfer.getAmount().getCurrency())
                        .setRegNoFromExt(sourceAccount.getAccountRegNoExt())
                        .setSavePayee(true)
                        .setTextFrom(transfer.getSourceMessage())
                        .setTextTo(transfer.getDestinationMessage());

        RegisterPaymentRequest registerPaymentRequest =
                RegisterPaymentRequest.create(
                        businessData, configuration.getLanguageCode(), transferType);

        RegisterPaymentResponse registerPaymentResponse =
                apiClient.registerPayment(registerPaymentRequest);

        return registerPaymentResponse.getOrderRef();
    }

    private String formatDate(Date date) {
        return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(date);
    }
}
