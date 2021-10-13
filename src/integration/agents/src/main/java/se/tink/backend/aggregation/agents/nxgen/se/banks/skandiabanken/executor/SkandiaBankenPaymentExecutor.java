package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.BankIdPolling;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.PaymentSourceAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities.UpcomingPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.rpc.FetchPaymentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.transfer.rpc.Transfer;

@AllArgsConstructor
public class SkandiaBankenPaymentExecutor implements PaymentExecutor {

    SkandiaBankenApiClient apiClient;
    SupplementalInformationController supplementalInformationController;

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {

        Collection<PaymentSourceAccount> paymentSourceAccounts =
                apiClient.fetchPaymentSourceAccount();

        PaymentSourceAccount sourceAccount =
                SkandiaBankenExecutorUtils.tryFindOwnAccount(
                        transfer.getSource(), paymentSourceAccounts);

        PaymentRequest paymentRequest =
                PaymentRequest.createPaymentRequest(transfer, sourceAccount);

        apiClient.submitPayment(addPaymentRequestToList(paymentRequest));

        FetchPaymentsResponse unapprovedPayments = apiClient.fetchUnapprovedPayments();

        ArrayList<String> paymentIdList =
                addEncryptedPaymentIdToList(unapprovedPayments, paymentRequest);

        signTransfer(paymentIdList);
    }

    private void signTransfer(ArrayList<String> paymentIdList) {
        String signReference = apiClient.initSignPayment(paymentIdList).getSignReference();

        supplementalInformationController.openMobileBankIdAsync(null);

        poll(signReference);

        apiClient.completePayment(paymentIdList, signReference);
    }

    private ArrayList<PaymentRequest> addPaymentRequestToList(PaymentRequest paymentRequest) {
        ArrayList<PaymentRequest> paymentRequestList = new ArrayList<>();
        paymentRequestList.add(paymentRequest);
        return paymentRequestList;
    }

    private ArrayList<String> addEncryptedPaymentIdToList(
            FetchPaymentsResponse unapprovedPayments, PaymentRequest paymentRequest) {
        UpcomingPaymentEntity payment = findPayment(unapprovedPayments, paymentRequest);

        ArrayList<String> paymentIdList = new ArrayList<>();
        paymentIdList.add(payment.getEncryptedPaymentId());
        return paymentIdList;
    }

    private UpcomingPaymentEntity findPayment(
            FetchPaymentsResponse unapprovedPayments, PaymentRequest paymentRequest) {
        return unapprovedPayments.stream()
                .filter(paymentEntity -> paymentEntity.isSamePayment(paymentRequest))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    public void poll(String signReference) {
        BankIdStatus status;

        Uninterruptibles.sleepUninterruptibly(BankIdPolling.INITIAL_SLEEP, TimeUnit.MILLISECONDS);
        for (int i = 0; i < BankIdPolling.MAX_ATTEMPTS; i++) {

            status = apiClient.pollPaymentSignStatus(signReference);

            switch (status) {
                case DONE:
                    return;
                case WAITING:
                    break;
                case CANCELLED:
                default:
                    throw new IllegalStateException();
            }

            Uninterruptibles.sleepUninterruptibly(
                    BankIdPolling.SLEEP_BETWEEN_POLLS, TimeUnit.MILLISECONDS);
        }
    }
}
