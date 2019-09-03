package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc.CreateDraftPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.utils.BunqRedirectHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.BunqTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class BunqPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private BunqApiClient apiClient;
    private SessionStorage sessionStorage;
    private BunqRedirectHandler bunqRedirectHandler;

    public BunqPaymentExecutor(
            SessionStorage sessionStorage,
            BunqApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
        this.bunqRedirectHandler = new BunqRedirectHandler(supplementalInformationHelper);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {

        String accountId = mapDebtorAccountToAccountId(paymentRequest.getPayment().getDebtor());
        CreateDraftPaymentRequest createDraftPaymentRequest =
                CreateDraftPaymentRequest.of(paymentRequest);

        long paymentId =
                apiClient
                        .createDraftPayment(
                                sessionStorage.get(StorageKeys.USER_ID),
                                accountId,
                                createDraftPaymentRequest)
                        .getId();

        return getDraftPaymentResponse(apiClient, accountId, paymentId);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {

        String accountId = mapDebtorAccountToAccountId(paymentRequest.getPayment().getDebtor());
        long paymentId = Long.parseLong(paymentRequest.getPayment().getUniqueId());

        return getDraftPaymentResponse(apiClient, accountId, paymentId);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentStatus paymentStatus;
        String nextStep;
        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                // Redirect the user to Bunq. User need to log-in and then approve the
                // draft-payment.
                // Bunq deeplinks information:
                // https://together.bunq.com/d/4538-deep-linking-to-payment-screen-with-url-scheme
                bunqRedirectHandler.handleRedirect(new URL(Urls.EVENTS_DEEP_LINK));

                String accountId =
                        mapDebtorAccountToAccountId(
                                paymentMultiStepRequest.getPayment().getDebtor());
                long paymentId = Long.parseLong(paymentMultiStepRequest.getPayment().getUniqueId());
                paymentStatus = poll(accountId, paymentId);
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }
        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {

        List<PaymentResponse> response = new ArrayList<>();

        for (PaymentRequest request : paymentListRequest.getPaymentRequestList()) {
            response.add(fetch(request));
        }

        return new PaymentListResponse(response);
    }

    private String mapDebtorAccountToAccountId(Debtor debtor) throws PaymentException {
        if (debtor.getAccountIdentifierType() != Type.IBAN) {
            throw new PaymentException(
                    "Unsupported debtor account identifier type : "
                            + debtor.getAccountIdentifierType().toString());
        }

        BunqTransactionalAccountFetcher accountFetcher =
                new BunqTransactionalAccountFetcher(sessionStorage, apiClient.getBaseApiClient());
        Collection<TransactionalAccount> transactionalAccounts = accountFetcher.fetchAccounts();
        TransactionalAccount matchedAccount =
                transactionalAccounts.stream()
                        .filter(
                                account ->
                                        account.getAccountNumber()
                                                .equals(debtor.getAccountNumber()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No account found matching the debtor's account."));

        return matchedAccount.getApiIdentifier();
    }

    private PaymentResponse getDraftPaymentResponse(
            BunqApiClient apiClient, String accountId, long paymentId) {
        return apiClient
                .getDraftPayment(sessionStorage.get(StorageKeys.USER_ID), accountId, paymentId)
                .getDraftPayment()
                .toTinkPaymentResponse();
    }

    private PaymentStatus poll(String accountId, long paymentId) throws PaymentException {
        PaymentStatus status;
        for (int i = 0; i < BunqConstants.Payment.MAX_POLL_ATTEMPTS; i++) {
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);

            status =
                    getDraftPaymentResponse(apiClient, accountId, paymentId)
                            .getPayment()
                            .getStatus();

            switch (status) {
                case UNDEFINED:
                case CREATED:
                case PENDING:
                    break;
                default:
                    return status;
            }
        }
        throw new PaymentException("Please confirm the draft-payment request with Bunq App!");
    }
}
