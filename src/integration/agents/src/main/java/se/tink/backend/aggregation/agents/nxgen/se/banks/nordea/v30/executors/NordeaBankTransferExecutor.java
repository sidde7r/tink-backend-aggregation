package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors;

import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.BankPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.InternalBankTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.InternalBankTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaBankTransferExecutor implements BankTransferExecutor {

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
        final Optional<PaymentEntity> payment = executorHelper.findInOutbox(transfer);

        if (payment.isPresent()) {
            executorHelper.confirm(payment.get().getApiIdentifier());
        } else {
            createNewTransfer(transfer);
        }
        return Optional.empty();
    }

    private void createNewTransfer(Transfer transfer) {
        final FetchAccountResponse accountResponse =
                Optional.ofNullable(apiClient.fetchAccount())
                        .orElseThrow(executorHelper::failedFetchAccountsError);

        final TransferMessageFormatter transferMessageFormatter =
                new TransferMessageFormatter(
                        catalog,
                        NordeaSEConstants.Transfer.TRANSFER_MESSAGE_LENGTH_CONFIG,
                        new StringNormalizerSwedish(
                                NordeaSEConstants.Transfer.WHITE_LISTED_CHARACTERS));

        // find source account
        final AccountEntity sourceAccount =
                executorHelper.validateSourceAccount(transfer, accountResponse, false);

        // find internal destination account
        final Optional<AccountEntity> destinationInternalAccount =
                executorHelper.validateOwnDestinationAccount(transfer, accountResponse);

        if (destinationInternalAccount.isPresent()) {
            // Internal transfers can be executed directly and doesn't need signing.
            executeInternalBankTransfer(
                    transfer,
                    sourceAccount,
                    destinationInternalAccount.get(),
                    transferMessageFormatter);
        } else {
            // create destination account
            final BeneficiariesEntity destinationExternalAccount =
                    executorHelper
                            .validateDestinationAccount(transfer)
                            .orElse(createDestinationAccount(transfer.getDestination()));
            executeExternalBankTransfer(
                    transfer, sourceAccount, destinationExternalAccount, transferMessageFormatter);
        }
    }

    private void executeInternalBankTransfer(
            Transfer transfer,
            AccountEntity sourceAccount,
            AccountEntity destinationInternalAccount,
            TransferMessageFormatter transferMessageFormatter) {

        InternalBankTransferRequest transferRequest = new InternalBankTransferRequest();
        transferRequest.setAmount(transfer);
        transferRequest.setFrom(sourceAccount);
        transferRequest.setTo(destinationInternalAccount);
        transferRequest.setMessage(transfer, transferMessageFormatter);
        transferRequest.setDue(transfer);

        InternalBankTransferResponse transferResponse =
                apiClient.executeInternalBankTransfer(transferRequest);

        if (!transferResponse.isTransferAccepted()) {
            throw executorHelper.transferFailedError();
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
                throw executorHelper.invalidDestError();
        }
        destinationAccount.setAccountNumber(
                accountIdentifier.getIdentifier(NORDEA_ACCOUNT_FORMATTER));
        accountIdentifier.getName().ifPresent(destinationAccount::setName);

        return destinationAccount;
    }

    private void executeExternalBankTransfer(
            Transfer transfer,
            AccountEntity sourceAccount,
            BeneficiariesEntity destinationAccount,
            TransferMessageFormatter transferMessageFormatter) {

        // create transfer request
        PaymentRequest transferRequest =
                createPaymentRequest(
                        transfer, sourceAccount, destinationAccount, transferMessageFormatter);

        // execute external transfer
        BankPaymentResponse transferResponse = apiClient.executeBankPayment(transferRequest);

        String transferId = transferResponse.getApiIdentifier();
        // confirm external transfer
        executorHelper.confirm(transferId);
    }

    private PaymentRequest createPaymentRequest(
            Transfer transfer,
            AccountEntity sourceAccount,
            BeneficiariesEntity destinationAccount,
            TransferMessageFormatter transferMessageFormatter) {

        PaymentRequest transferRequest = new PaymentRequest();
        transferRequest.setAmount(transfer.getAmount());
        transferRequest.setFrom(sourceAccount);
        transferRequest.setBankName(destinationAccount);
        transferRequest.setTo(destinationAccount);
        transferRequest.setMessage(transfer, transferMessageFormatter);
        transferRequest.setDue(transfer.getDueDate());
        transferRequest.setType(NordeaSEConstants.PaymentTypes.LBAN);
        transferRequest.setToAccountNumberType(getToAccountType(transfer));

        return transferRequest;
    }

    private String getToAccountType(Transfer transfer) {
        switch (transfer.getDestination().getType()) {
            case SE_NDA_SSN:
                return NordeaSEConstants.PaymentAccountTypes.NDASE;
            case SE:
                if (transfer.getDestination()
                        .to(SwedishIdentifier.class)
                        .getBank()
                        .equals(Bank.NORDEA_PERSONKONTO)) {
                    return NordeaSEConstants.PaymentAccountTypes.NDASE;
                } else {
                    return NordeaSEConstants.PaymentAccountTypes.LBAN;
                }
            default:
                return NordeaSEConstants.PaymentAccountTypes.LBAN;
        }
    }
}
