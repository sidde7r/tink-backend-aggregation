package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors;

import static io.vavr.Predicates.not;

import com.google.common.base.Strings;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.BankPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.InternalBankTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.InternalBankTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.utilities.NordeaDateUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.utils.accountidentifier.IntraBankChecker;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaBankTransferExecutor implements BankTransferExecutor {

    private static final Logger log = LoggerFactory.getLogger(NordeaBankTransferExecutor.class);
    private static final NordeaAccountIdentifierFormatter NORDEA_ACCOUNT_FORMATTER =
            new NordeaAccountIdentifierFormatter();

    private final Catalog catalog;
    private NordeaBaseApiClient apiClient;
    private NordeaExecutorHelper executorHelper;

    public NordeaBankTransferExecutor(
            NordeaBaseApiClient apiClient, Catalog catalog, NordeaExecutorHelper executorHelper) {
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
                    transfer, sourceAccount, internalAccount.get(), dueDate);
        } else {
            // Transfers require adding beneficiary and signing.
            executeTransferWithUserSigning(transfer, sourceAccount, dueDate);
        }
    }

    private void executeTransferWithoutUserSigning(
            Transfer transfer,
            AccountEntity sourceAccount,
            AccountEntity destinationInternalAccount,
            Date dueDate) {
        InternalBankTransferRequest transferRequest =
                InternalBankTransferRequest.builder()
                        .amount(transfer.getAmount().getValue().intValue())
                        .from(sourceAccount.formatAccountNumber())
                        .to(destinationInternalAccount.formatAccountNumber())
                        .message(getTransferMessage(transfer, true))
                        .due(dueDate)
                        .build();

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
            Transfer transfer, AccountEntity sourceAccount, Date dueDate) {
        final BeneficiariesEntity destinationAccount =
                executorHelper
                        .validateDestinationAccount(transfer)
                        .orElse(createDestinationAccount(transfer.getDestination()));

        // create transfer request
        PaymentRequest transferRequest =
                createExternalTransferRequest(transfer, sourceAccount, destinationAccount, dueDate);

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

    private PaymentRequest createExternalTransferRequest(
            Transfer transfer,
            AccountEntity sourceAccount,
            BeneficiariesEntity destinationAccount,
            Date dueDate) {
        return PaymentRequest.builder()
                .amount(transfer.getAmount().getValue())
                .currency(transfer.getAmount().getCurrency())
                .from(sourceAccount.formatAccountNumber())
                .bankName(destinationAccount.getBankName())
                .to(destinationAccount.getAccountNumber())
                .recipientName(destinationAccount.getName())
                .message(getTransferMessage(transfer, false))
                .due(dueDate)
                .type(NordeaBaseConstants.PaymentTypes.LBAN)
                .toAccountNumberType(getToAccountType(transfer))
                .build();
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
                return NordeaBaseConstants.PaymentAccountTypes.NDASE;
            case SE:
                if (EnumSet.of(Bank.NORDEA_PERSONKONTO, Bank.NORDEA)
                        .contains(
                                transfer.getDestination().to(SwedishIdentifier.class).getBank())) {
                    return NordeaBaseConstants.PaymentAccountTypes.NDASE;
                } else {
                    return NordeaBaseConstants.PaymentAccountTypes.LBAN;
                }
            default:
                return NordeaBaseConstants.PaymentAccountTypes.LBAN;
        }
    }

    private void handleTransferErrors(HttpResponseException e) {
        final ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
        errorResponse.throwAppropriateErrorIfAny();
        log.warn("Transfer execution failed", e);
        throw e;
    }

    private String getTransferMessage(
            Transfer transfer, boolean isTransferBetweenSameUserAccounts) {
        final TransferMessageFormatter transferMessageFormatter =
                new TransferMessageFormatter(
                        catalog,
                        NordeaBaseConstants.Transfer.TRANSFER_MESSAGE_LENGTH_CONFIG,
                        new StringNormalizerSwedish(
                                NordeaBaseConstants.Transfer.WHITE_LISTED_CHARACTERS));

        return Optional.ofNullable(transfer)
                .map(t -> t.getRemittanceInformation().getValue())
                .filter(not(Strings::isNullOrEmpty))
                .map(
                        s ->
                                transferMessageFormatter
                                        .getDestinationMessageFromRemittanceInformation(
                                                transfer, isTransferBetweenSameUserAccounts))
                .orElse("");
    }
}
