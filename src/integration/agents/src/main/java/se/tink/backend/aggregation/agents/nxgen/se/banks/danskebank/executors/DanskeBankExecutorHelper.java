package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.TransferAccountType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.TransferType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity.BusinessDataEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.SignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ValidatePaymentDateRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ValidatePaymentDateResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.DanskeBankWebDriverHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class DanskeBankExecutorHelper {

    private final DanskeBankSEApiClient apiClient;
    private final String deviceId;
    private final DanskeBankSEConfiguration configuration;
    private final SupplementalRequester supplementalRequester;

    public DanskeBankExecutorHelper(
            DanskeBankSEApiClient apiClient,
            String deviceId,
            DanskeBankConfiguration configuration,
            SupplementalRequester supplementalRequester) {
        this.apiClient = apiClient;
        this.deviceId = deviceId;
        this.configuration = (DanskeBankSEConfiguration) configuration;
        this.supplementalRequester = supplementalRequester;
    }

    public Date validatePaymentDate(Transfer transfer, boolean isOwnDestinationAccount) {
        String transferType =
                isOwnDestinationAccount
                        ? TransferAccountType.INTERNAL
                        : TransferAccountType.EXTERNAL;

        ValidatePaymentDateRequest paymentDateRequest =
                ValidatePaymentDateRequest.builder()
                        .bookingDate(transfer.getDueDate())
                        .countryCode(configuration.getMarketCode())
                        .isCurrencyTransaction(false)
                        .payType("")
                        .receiverAccount(transfer.getDestination().getIdentifier())
                        .transferType(transferType)
                        .build();

        ValidatePaymentDateResponse paymentDateResponse =
                apiClient.validatePaymentDate(paymentDateRequest);

        // Case when we get a dueDate from the user and bank sends another date since the user's
        // date is not valid.
        if (transfer.getDueDate() != null
                && !paymentDateResponse.isTransferDateSameAsBookingDate(transfer.getDueDate())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(EndUserMessage.INVALID_DUEDATE_TOO_SOON_OR_NOT_BUSINESSDAY)
                    .setInternalStatus(InternalStatus.INVALID_DUE_DATE.toString())
                    .build();
        }

        return paymentDateResponse.getBookingDate();
    }

    public String getTransferType(boolean isOwnDestinationAccount) {
        return isOwnDestinationAccount ? TransferType.INTERNAL : TransferType.EXTERNAL;
    }

    public RegisterPaymentResponse registerExternalTransfer(
            Transfer transfer,
            ListAccountsResponse accounts,
            String creditorName,
            String creditorBankName,
            Date paymentDate,
            boolean isOwnDestinationAccount,
            TransferMessageFormatter transferMessageFormatter) {
        AccountEntity sourceAccount = accounts.findAccount(transfer.getSource().getIdentifier());

        String transferType = getTransferType(isOwnDestinationAccount);

        String destinationAccountName =
                Strings.isNullOrEmpty(creditorName)
                        ? transfer.getDestination().getIdentifier()
                        : creditorName;

        BusinessDataEntity businessData =
                BusinessDataEntity.builder()
                        .eInvoiceMarking(false)
                        .accountNameFrom(sourceAccount.getAccountName())
                        .accountNameTo(destinationAccountName)
                        .accountNoExtFrom(sourceAccount.getAccountNoExt())
                        .accountNoIntFrom(sourceAccount.getAccountNoInt())
                        .accountNoToExt(transfer.getDestination().getIdentifier())
                        .accountProductFrom(sourceAccount.getAccountProduct())
                        .allowDuplicateTransfer(true)
                        .amount(transfer.getAmount().getValue())
                        .bankName(creditorBankName)
                        .bookingDate(formatDate(paymentDate))
                        .currency(transfer.getAmount().getCurrency())
                        .regNoFromExt(sourceAccount.getAccountRegNoExt())
                        .savePayee(false)
                        .textFrom(
                                transfer.getSourceMessage() != null
                                        ? transferMessageFormatter.getSourceMessage(transfer)
                                        : null)
                        .textTo(
                                transferMessageFormatter
                                        .getDestinationMessageFromRemittanceInformation(
                                                transfer, isOwnDestinationAccount))
                        .build();

        return apiClient.registerPayment(
                RegisterPaymentRequest.builder()
                        .businessData(businessData)
                        .language(configuration.getLanguageCode())
                        .signatureType(transferType)
                        .build());
    }

    public RegisterPaymentResponse registerOwnDestinationAccountTransfer(
            Transfer transfer,
            ListAccountsResponse accounts,
            AccountEntity ownDestinationAccount,
            Date paymentDate,
            boolean isOwnDestinationAccount,
            TransferMessageFormatter transferMessageFormatter) {
        AccountEntity sourceAccount = accounts.findAccount(transfer.getSource().getIdentifier());

        String transferType = getTransferType(isOwnDestinationAccount);

        BusinessDataEntity businessData =
                BusinessDataEntity.builder()
                        .accountNameFrom(sourceAccount.getAccountName())
                        .accountNameTo(ownDestinationAccount.getAccountName())
                        .accountNoExtFrom(sourceAccount.getAccountNoExt())
                        .accountNoIntFrom(sourceAccount.getAccountNoInt())
                        .accountNoIntTo(ownDestinationAccount.getAccountNoInt())
                        .accountNoToExt(transfer.getDestination().getIdentifier())
                        .allowDuplicateTransfer(true)
                        .amount(transfer.getAmount().getValue())
                        .bankName(null)
                        .bookingDate(formatDate(paymentDate))
                        .currency(transfer.getAmount().getCurrency())
                        .forcableErrorsRC("0000")
                        .textFrom(
                                transfer.getSourceMessage() != null
                                        ? transferMessageFormatter.getSourceMessage(transfer)
                                        : null)
                        .textTo(
                                transferMessageFormatter
                                        .getDestinationMessageFromRemittanceInformation(
                                                transfer, isOwnDestinationAccount))
                        .build();

        return apiClient.registerPayment(
                RegisterPaymentRequest.builder()
                        .businessData(businessData)
                        .language(configuration.getLanguageCode())
                        .signatureType(transferType)
                        .build());
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
        return apiClient
                .signPayment(
                        SignRequest.builder()
                                .reference(reference)
                                .mode("Sign")
                                .channel("0")
                                .build())
                .getBankIdStatus();
    }

    public String getSignaturePackage(
            HttpResponse injectJsCheckStep, String username, String signText) {
        String dynamicBankIdSignJavascript =
                DanskeBankConstants.Javascript.getDeviceInfo(
                                deviceId,
                                configuration.getMarketCode(),
                                configuration.getAppName(),
                                configuration.getAppVersion())
                        + injectJsCheckStep.getBody(String.class);

        // Execute javascript to get encrypted signature package and finalize package
        WebDriver driver = null;
        try {
            driver = DanskeBankWebDriverHelper.constructWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createSignSEBankIdJavascript(
                            dynamicBankIdSignJavascript, username, signText));
            return driver.findElement(By.tagName("body")).getAttribute("signaturePackage");
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private TransferExecutionException bankIdTimeoutError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.BANKID_NO_RESPONSE.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_NO_RESPONSE)
                .setInternalStatus(InternalStatus.BANKID_NO_RESPONSE.toString())
                .build();
    }

    private TransferExecutionException bankIdCancelledError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.BANKID_CANCELLED.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_CANCELLED)
                .setInternalStatus(InternalStatus.BANKID_CANCELLED.toString())
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
                .setInternalStatus(InternalStatus.BANKID_ANOTHER_IN_PROGRESS.toString())
                .build();
    }

    private String formatDate(Date date) {
        return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(date);
    }
}
