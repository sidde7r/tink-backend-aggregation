package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors;

import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.BankPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ConfirmTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

/**
 * Modifying a payment in the Nordea app will generate a new payment id for that payment in the
 * outbox.
 */
public class NordeaPaymentExecutor implements PaymentExecutor {
    private final Catalog catalog;
    private NordeaSEApiClient apiClient;
    private NordeaExecutorHelper executorHelper;

    public NordeaPaymentExecutor(
            NordeaSEApiClient apiClient, Catalog catalog, NordeaExecutorHelper executorHelper) {
        this.apiClient = apiClient;
        this.catalog = catalog;
        this.executorHelper = executorHelper;
    }

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        // check if transfer already exist in outbox, if it does then user can confirm that one
        isInOutbox(transfer);
    }

    /**
     * Check if payment already exist in outbox as unconfirmed if it does then execute a payment on
     * that id instead. Otherwise, proceeds to create a new payment
     */
    private void isInOutbox(Transfer transfer) {
        final Optional<EInvoiceEntity> payment =
                apiClient.fetchEInvoice().getEInvoices().stream()
                        .filter(EInvoiceEntity::isPayment)
                        .filter(EInvoiceEntity::isUnconfirmed)
                        .filter(eInvoiceEntity -> eInvoiceEntity.isEqualToTransfer(transfer))
                        .findFirst();

        if (payment.isPresent()) {
            String paymentId = payment.get().getId();
            ConfirmTransferRequest confirmTransferRequest = new ConfirmTransferRequest(paymentId);
            executorHelper.confirm(confirmTransferRequest, paymentId);
            return;
        }
        createNewPayment(transfer);
    }

    private BeneficiariesEntity createDestination(Transfer transfer) {
        return executorHelper
                // create plusgiro or bankgiro destination if it
                // does not exist in beneficiaries
                .createRecipient(transfer)
                // throw exception if destination does not exist
                .orElseThrow(() -> executorHelper.throwInvalidDestError());
    }

    private void createNewPayment(Transfer transfer) {
        final FetchAccountResponse accountResponse = fetchAccounts();

        // find source account
        final AccountEntity sourceAccount =
                executorHelper.validateSourceAccount(transfer, accountResponse, true);

        // find destination in beneficiaries
        final BeneficiariesEntity destinationAccount =
                executorHelper
                        .validateDestinationAccount(transfer)
                        .orElseGet(() -> createDestination(transfer));

        // create request
        final PaymentRequest paymentRequest =
                createPaymentRequest(transfer, sourceAccount, destinationAccount);

        // execute payment
        executeBankPayment(paymentRequest);
    }

    private FetchAccountResponse fetchAccounts() {
        FetchAccountResponse accountResponse = apiClient.fetchAccount();

        if (accountResponse == null) {
            executorHelper.throwFailedFetchAccountsError();
        }

        return accountResponse;
    }

    private PaymentRequest createPaymentRequest(
            Transfer transfer,
            AccountEntity sourceAccount,
            BeneficiariesEntity destinationAccount) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(transfer);
        paymentRequest.setFrom(sourceAccount);
        paymentRequest.setBankName(destinationAccount);
        paymentRequest.setTo(destinationAccount);
        paymentRequest.setMessage(transfer.getDestinationMessage());
        paymentRequest.setDue(transfer);
        paymentRequest.setType(executorHelper.getPaymentType(transfer.getDestination()));
        paymentRequest.setToAccountNumberType(
                executorHelper.getPaymentAccountType(transfer.getDestination()));

        return paymentRequest;
    }

    private void executeBankPayment(PaymentRequest paymentRequest) {
        try {
            BankPaymentResponse paymentResponse = apiClient.executeBankPayment(paymentRequest);
            String paymentId = paymentResponse.getId();
            ConfirmTransferRequest confirmTransferRequest = new ConfirmTransferRequest(paymentId);
            executorHelper.confirm(confirmTransferRequest, paymentId);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                executorHelper.throwPaymentFailedError();
            }
        }
    }
}
