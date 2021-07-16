package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors;

import java.util.Date;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.BankPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.utilities.NordeaDateUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.transfer.rpc.Transfer;

/**
 * Modifying a payment in the Nordea app will generate a new payment id for that payment in the
 * outbox.
 */
public class NordeaPaymentExecutor implements PaymentExecutor {

    private static final Logger log = LoggerFactory.getLogger(NordeaPaymentExecutor.class);
    private static final NordeaAccountIdentifierFormatter NORDEA_ACCOUNT_FORMATTER =
            new NordeaAccountIdentifierFormatter();
    private NordeaBaseApiClient apiClient;
    private NordeaExecutorHelper executorHelper;

    public NordeaPaymentExecutor(
            NordeaBaseApiClient apiClient, NordeaExecutorHelper executorHelper) {
        this.apiClient = apiClient;
        this.executorHelper = executorHelper;
    }

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        Date dueDate = NordeaDateUtil.getTransferDateForBgPg(transfer.getDueDate());
        try {
            final Optional<PaymentEntity> payment = executorHelper.findInOutbox(transfer, dueDate);

            if (payment.isPresent()) {
                executorHelper.confirm(payment.get().getApiIdentifier());
            } else {
                createNewPayment(transfer, dueDate);
            }
        } catch (HttpResponseException e) {
            final ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
            errorResponse.throwAppropriateErrorIfAny();
            log.warn("Payment execution failed", e);
            throw e;
        }
    }

    private BeneficiariesEntity createDestination(Transfer transfer) {
        BeneficiariesEntity destination = new BeneficiariesEntity();
        destination.setAccountNumber(
                transfer.getDestination().getIdentifier(NORDEA_ACCOUNT_FORMATTER));
        transfer.getDestination().getName().ifPresent(destination::setName);
        return destination;
    }

    private void createNewPayment(Transfer transfer, Date dueDate) {
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
                createPaymentRequest(transfer, sourceAccount, destinationAccount, dueDate);

        // execute payment
        executeBankPayment(paymentRequest);
    }

    private FetchAccountResponse fetchAccounts() {
        FetchAccountResponse accountResponse = apiClient.fetchAccount();

        if (accountResponse == null) {
            throw ErrorResponse.failedFetchAccountsError();
        }

        return accountResponse;
    }

    private PaymentRequest createPaymentRequest(
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
                .message(transfer.getRemittanceInformation().getValue())
                .due(dueDate)
                .type(executorHelper.getPaymentType(transfer.getDestination()))
                .toAccountNumberType(
                        executorHelper.getPaymentAccountType(transfer.getDestination()))
                .build();
    }

    private void executeBankPayment(PaymentRequest paymentRequest) {
        try {
            BankPaymentResponse paymentResponse = apiClient.executeBankPayment(paymentRequest);
            executorHelper.confirm(paymentResponse.getApiIdentifier());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST
                    || e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                final ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
                errorResponse.throwAppropriateErrorIfAny();
                log.warn("Payment execution failed", e);
                throw e;
            }
            throw e;
        }
    }
}
