package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.transfer;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class DanskeBankSETransferExecutor implements BankTransferExecutor {

    private final DanskeBankSEApiClient apiClient;
    private final String deviceId;
    private final DanskeBankSEConfiguration configuration;
    private final SupplementalRequester supplementalRequester;

    private String dynamicBankIdJavascript;

    public DanskeBankSETransferExecutor(
            DanskeBankSEApiClient apiClient,
            String deviceId,
            DanskeBankConfiguration configuration,
            SupplementalRequester supplementalRequester) {
        this.apiClient = apiClient;
        this.deviceId = deviceId;
        this.configuration = (DanskeBankSEConfiguration) configuration;
        this.supplementalRequester = supplementalRequester;
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

        RegisterPaymentResponse registerPaymentResponse =
                registerPayment(transfer, accounts, creditorName, creditorBankName, paymentDate);

        signPayment(registerPaymentResponse);

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

    private RegisterPaymentResponse registerPayment(
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

        String accountName =
                Strings.isNullOrEmpty(creditorName.getCreditorName())
                        ? transfer.getDestination().getIdentifier()
                        : creditorName.getCreditorName();

        BusinessDataEntity businessData =
                new BusinessDataEntity()
                        .seteInvoiceMarking(false)
                        .setAccountNameFrom(sourceAccount.getAccountName())
                        .setAccountNameTo(accountName)
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
                        .setSavePayee(false)
                        .setTextFrom(transfer.getSourceMessage())
                        .setTextTo(transfer.getRemittanceInformation().getValue());

        RegisterPaymentRequest registerPaymentRequest =
                RegisterPaymentRequest.create(
                        businessData, configuration.getLanguageCode(), transferType);

        RegisterPaymentResponse registerPaymentResponse =
                apiClient.registerPayment(registerPaymentRequest);

        return registerPaymentResponse;
    }

    public void signPayment(RegisterPaymentResponse registerPaymentResponse) {
        supplementalRequester.openBankId(registerPaymentResponse.getAutoStartToken(), false);
        poll(registerPaymentResponse.getOrderRef());
    }

    public void poll(String reference) {
        BankIdStatus status;

        for (int i = 0; i < DanskeBankConstants.Transfer.MAX_POLL_ATTEMPTS; i++) {
            try {
                status = collect(reference);

                switch (status) {
                    case DONE:
                        return;
                    case WAITING:
                        break;
                    case CANCELLED:
                        throw bankIdCancelledError();
                    case TIMEOUT:
                        throw bankIdTimeoutError();
                    case INTERRUPTED:
                        throw bankIdInterruptedError();
                    default:
                        throw bankIdFailedError();
                }

                Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);

            } catch (HttpResponseException | IllegalStateException e) {
                throw bankIdFailedError();
            }
        }

        throw bankIdTimeoutError();
    }

    private BankIdStatus collect(String reference) {
        return apiClient.signPayment(reference).getBankIdStatus();
    }

    private TransferExecutionException bankIdTimeoutError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.BANKID_NO_RESPONSE.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_NO_RESPONSE)
                .build();
    }

    private TransferExecutionException bankIdCancelledError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.BANKID_CANCELLED.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_CANCELLED)
                .build();
    }

    private TransferExecutionException bankIdFailedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(EndUserMessage.BANKID_TRANSFER_FAILED.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_TRANSFER_FAILED)
                .build();
    }

    private TransferExecutionException bankIdInterruptedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.BANKID_ANOTHER_IN_PROGRESS.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_ANOTHER_IN_PROGRESS)
                .build();
    }

    private String formatDate(Date date) {
        return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(date);
    }
}
