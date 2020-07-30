package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors;

import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.BankPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.InternalBankTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.InternalBankTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.utilities.NordeaDateUtil;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.utils.accountidentifier.IntraBankChecker;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaBankTransferExecutor implements BankTransferExecutor {

    private static final Logger log = LoggerFactory.getLogger(NordeaBankTransferExecutor.class);
    private static final NordeaAccountIdentifierFormatter NORDEA_ACCOUNT_FORMATTER =
            new NordeaAccountIdentifierFormatter();

    private final Catalog catalog;
    private NordeaSEApiClient apiClient;
    private NordeaExecutorHelper executorHelper;

    public NordeaBankTransferExecutor(
            NordeaSEApiClient apiClient, Catalog catalog, NordeaExecutorHelper executorHelper) {
        this.apiClient = apiClient;
        this.catalog = catalog;
        this.executorHelper = executorHelper;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        Date dueDate = getDueDate(transfer);
        try {
            final Optional<PaymentEntity> payment = executorHelper.findInOutbox(transfer, dueDate);

            if (payment.isPresent()) {
                executorHelper.confirm(payment.get().getApiIdentifier());
            } else {
                createNewTransfer(transfer, dueDate);
            }
        } catch (HttpResponseException e) {
            handleTransferErrors(e);
        }
        return Optional.empty();
    }

    private void createNewTransfer(Transfer transfer, Date dueDate) {
        final FetchAccountResponse accountResponse =
                Optional.ofNullable(apiClient.fetchAccount())
                        .orElseThrow(ErrorResponse::failedFetchAccountsError);

        final TransferMessageFormatter transferMessageFormatter =
                new TransferMessageFormatter(
                        catalog,
                        NordeaSEConstants.Transfer.TRANSFER_MESSAGE_LENGTH_CONFIG,
                        new StringNormalizerSwedish(
                                NordeaSEConstants.Transfer.WHITE_LISTED_CHARACTERS));

        // minimum valid amount is 1 SEK
        executorHelper.validateMinimumTransferAmount(transfer);

        // find source account
        final AccountEntity sourceAccount =
                executorHelper.validateSourceAccount(transfer, accountResponse, false);

        // find if it's user's own account
        final Optional<AccountEntity> internalAccount =
                executorHelper.validateOwnDestinationAccount(transfer, accountResponse);

        if (internalAccount.isPresent()) {
            // Transfers doesn't need signing.
            executeTransferWithoutUserSigning(
                    transfer, sourceAccount, internalAccount.get(), transferMessageFormatter);
        } else {
            // Transfers require adding beneficiary and signing.
            executeTransferWithUserSigning(
                    transfer, sourceAccount, transferMessageFormatter, dueDate);
        }
    }

    private void executeTransferWithoutUserSigning(
            Transfer transfer,
            AccountEntity sourceAccount,
            AccountEntity destinationInternalAccount,
            TransferMessageFormatter transferMessageFormatter) {

        InternalBankTransferRequest transferRequest = new InternalBankTransferRequest();
        transferRequest.setAmount(transfer);
        transferRequest.setFrom(sourceAccount);
        transferRequest.setTo(destinationInternalAccount);
        transferRequest.setMessage(transfer, transferMessageFormatter);
        transferRequest.setDue(
                ThreadSafeDateFormat.FORMATTER_DAILY.format(
                        NordeaDateUtil.getTransferDateForIntraBankTransfer(transfer.getDueDate())));

        InternalBankTransferResponse transferResponse =
                apiClient.executeInternalBankTransfer(transferRequest);

        if (!transferResponse.isTransferAccepted()) {
            throw ErrorResponse.transferFailedError(null);
        }
    }

    private BeneficiariesEntity createDestinationAccount(AccountIdentifier accountIdentifier) {
        BeneficiariesEntity destinationAccount = new BeneficiariesEntity();

        switch (accountIdentifier.getType()) {
            case SE:
                destinationAccount.setBankName(
                        ((SwedishIdentifier) accountIdentifier).getBankName());
                break;
            case SE_NDA_SSN:
                destinationAccount.setBankName(Bank.NORDEA_PERSONKONTO.getDisplayName());
                break;
            default:
                throw ErrorResponse.invalidDestError();
        }
        destinationAccount.setAccountNumber(
                accountIdentifier.getIdentifier(NORDEA_ACCOUNT_FORMATTER));
        accountIdentifier.getName().ifPresent(destinationAccount::setName);

        return destinationAccount;
    }

    private void executeTransferWithUserSigning(
            Transfer transfer,
            AccountEntity sourceAccount,
            TransferMessageFormatter transferMessageFormatter,
            Date dueDate) {
        final BeneficiariesEntity destinationAccount =
                executorHelper
                        .validateDestinationAccount(transfer)
                        .orElse(createDestinationAccount(transfer.getDestination()));

        // create transfer request
        PaymentRequest transferRequest =
                createPaymentRequest(
                        transfer,
                        sourceAccount,
                        destinationAccount,
                        transferMessageFormatter,
                        dueDate);

        // execute external transfer
        try {
            BankPaymentResponse transferResponse = apiClient.executeBankPayment(transferRequest);
            String transferId = transferResponse.getApiIdentifier();
            // confirm external transfer
            executorHelper.confirm(transferId);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                final ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
                errorResponse.throwAppropriateErrorIfAny();
                log.warn("Payment execution failed", e);
                throw e;
            }
            throw e;
        }
    }

    private PaymentRequest createPaymentRequest(
            Transfer transfer,
            AccountEntity sourceAccount,
            BeneficiariesEntity destinationAccount,
            TransferMessageFormatter transferMessageFormatter,
            Date dueDate) {
        PaymentRequest transferRequest = new PaymentRequest();
        transferRequest.setAmount(transfer.getAmount());
        transferRequest.setFrom(sourceAccount);
        transferRequest.setBankName(destinationAccount);
        transferRequest.setTo(destinationAccount);
        transferRequest.setMessage(transfer, transferMessageFormatter);
        transferRequest.setDue(dueDate);
        transferRequest.setType(NordeaSEConstants.PaymentTypes.LBAN);
        transferRequest.setToAccountNumberType(getToAccountType(transfer));

        return transferRequest;
    }

    private Date getDueDate(Transfer transfer) {
        if (IntraBankChecker.isSwedishMarketIntraBank(
                transfer.getSource(), transfer.getDestination())) {
            return NordeaDateUtil.getTransferDateForIntraBankTransfer(transfer.getDueDate());
        } else {
            return NordeaDateUtil.getTransferDateForInterBankTransfer(transfer.getDueDate());
        }
    }

    private String getToAccountType(Transfer transfer) {
        switch (transfer.getDestination().getType()) {
            case SE_NDA_SSN:
                return NordeaSEConstants.PaymentAccountTypes.NDASE;
            case SE:
                if (EnumSet.of(Bank.NORDEA_PERSONKONTO, Bank.NORDEA)
                        .contains(
                                transfer.getDestination().to(SwedishIdentifier.class).getBank())) {
                    return NordeaSEConstants.PaymentAccountTypes.NDASE;
                } else {
                    return NordeaSEConstants.PaymentAccountTypes.LBAN;
                }
            default:
                return NordeaSEConstants.PaymentAccountTypes.LBAN;
        }
    }

    private void handleTransferErrors(HttpResponseException e) {
        final ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
        errorResponse.throwAppropriateErrorIfAny();
        log.warn("Transfer execution failed", e);
        throw e;
    }
}
