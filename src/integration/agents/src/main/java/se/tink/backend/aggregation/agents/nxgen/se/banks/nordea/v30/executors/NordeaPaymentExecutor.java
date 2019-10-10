package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors;

import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.BankPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.transfer.rpc.Transfer;

/**
 * Modifying a payment in the Nordea app will generate a new payment id for that payment in the
 * outbox.
 */
public class NordeaPaymentExecutor implements PaymentExecutor {

    private static final Logger log = LoggerFactory.getLogger(NordeaPaymentExecutor.class);
    private static final NordeaAccountIdentifierFormatter NORDEA_ACCOUNT_FORMATTER =
            new NordeaAccountIdentifierFormatter();
    private NordeaSEApiClient apiClient;
    private NordeaExecutorHelper executorHelper;

    public NordeaPaymentExecutor(NordeaSEApiClient apiClient, NordeaExecutorHelper executorHelper) {
        this.apiClient = apiClient;
        this.executorHelper = executorHelper;
    }

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        // check if transfer already exist in outbox, if it does then user can confirm that one
        createNewOrConfirmExisting(transfer);
    }

    /**
     * Check if payment already exist in outbox as unconfirmed if it does then execute a payment on
     * that id instead. Otherwise, proceeds to create a new payment
     */
    private void createNewOrConfirmExisting(Transfer transfer) {
        try {
            final Optional<PaymentEntity> payment = executorHelper.findInOutbox(transfer);

            if (payment.isPresent()) {
                executorHelper.confirm(payment.get().getApiIdentifier());
            } else {
                createNewPayment(transfer);
            }
        } catch (HttpResponseException e) {
            log.warn("Payment execution failed", e);
            throw executorHelper.paymentFailedError(e);
        }
    }

    private BeneficiariesEntity createDestination(Transfer transfer) {
        BeneficiariesEntity destination = new BeneficiariesEntity();
        destination.setAccountNumber(
                transfer.getDestination().getIdentifier(NORDEA_ACCOUNT_FORMATTER));
        transfer.getDestination().getName().ifPresent(destination::setName);
        return destination;
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
            throw executorHelper.failedFetchAccountsError();
        }

        return accountResponse;
    }

    private PaymentRequest createPaymentRequest(
            Transfer transfer,
            AccountEntity sourceAccount,
            BeneficiariesEntity destinationAccount) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(transfer.getAmount());
        paymentRequest.setFrom(sourceAccount);
        paymentRequest.setBankName(destinationAccount);
        paymentRequest.setTo(destinationAccount);
        paymentRequest.setMessage(transfer.getDestinationMessage());
        paymentRequest.setDue(transfer.getDueDate());
        paymentRequest.setType(executorHelper.getPaymentType(transfer.getDestination()));
        paymentRequest.setToAccountNumberType(
                executorHelper.getPaymentAccountType(transfer.getDestination()));

        return paymentRequest;
    }

    private void executeBankPayment(PaymentRequest paymentRequest) {
        try {
            BankPaymentResponse paymentResponse = apiClient.executeBankPayment(paymentRequest);
            executorHelper.confirm(paymentResponse.getApiIdentifier());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                final ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
                if (errorResponse.isDuplicatePayment()) {
                    throw executorHelper.duplicatePaymentError(e);
                }
                log.warn("Payment execution failed", e);
                throw executorHelper.paymentFailedError(e);
            }
            throw e;
        }
    }
}
