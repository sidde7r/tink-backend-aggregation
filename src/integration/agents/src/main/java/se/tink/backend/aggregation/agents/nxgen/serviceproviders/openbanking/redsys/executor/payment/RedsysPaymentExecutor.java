package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Links;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.enums.RedsysTransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.sca.ScaRedirectCallbackHandler;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class RedsysPaymentExecutor implements PaymentExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(RedsysPaymentExecutor.class);
    private final RedsysApiClient apiClient;
    private final ScaRedirectCallbackHandler scaRedirectHandler;

    public RedsysPaymentExecutor(
            RedsysApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.scaRedirectHandler =
                new ScaRedirectCallbackHandler(supplementalInformationHelper, 30, TimeUnit.SECONDS);
    }

    private PaymentProduct paymentProductForPayment(Payment payment) throws PaymentException {
        if ((payment.getCreditor().getAccountIdentifierType() != Type.IBAN)
                || (payment.getDebtor().getAccountIdentifierType() != Type.IBAN)) {
            throw new PaymentException("Account types must be IBAN.");
        }
        if (payment.isSepa()) {
            return PaymentProduct.SEPA_INSTANT_TRANSFER;
        } else {
            return PaymentProduct.CROSS_BORDER;
        }
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();
        final PaymentProduct product = paymentProductForPayment(payment);
        final AccountReferenceEntity creditorAccount =
                AccountReferenceEntity.ofIban(payment.getCreditor().getAccountNumber());
        final AccountReferenceEntity debtorAccount =
                AccountReferenceEntity.ofIban(payment.getDebtor().getAccountNumber());

        CreatePaymentRequest.Builder requestBuilder =
                new CreatePaymentRequest.Builder()
                        .withAmount(payment.getAmount())
                        .withCreditorAccount(creditorAccount)
                        .withDebtorAccount(debtorAccount)
                        .withCreditorName(payment.getCreditor().getName())
                        .withRequestedExecutionDate(payment.getExecutionDate());

        final Reference reference = payment.getReference();
        if (reference != null && !Strings.isNullOrEmpty(reference.getValue())) {
            requestBuilder.withRemittanceInformation(reference.getValue());
        }

        final CreatePaymentRequest request = requestBuilder.build();
        final String scaToken = UUID.randomUUID().toString();
        final CreatePaymentResponse response = apiClient.createPayment(request, product, scaToken);

        Storage paymentStorage = new Storage();
        paymentStorage.put(StorageKeys.SCA_STATE, scaToken);
        final Optional<LinkEntity> scaRedirectLink = response.getLink(Links.SCA_REDIRECT);
        if (scaRedirectLink.isPresent()) {
            paymentStorage.put(StorageKeys.SCA_REDIRECT, scaRedirectLink.get().getHref());
        } else {
            LOG.warn("No scaRedirect for payment.");
        }

        return new PaymentResponse(
                new Payment.Builder()
                        .withCreditor(payment.getCreditor())
                        .withDebtor(payment.getDebtor())
                        .withAmount(payment.getAmount())
                        .withExecutionDate(payment.getExecutionDate())
                        .withCurrency(payment.getAmount().getCurrency())
                        .withUniqueId(response.getPaymentId())
                        .withType(product.getPaymentType())
                        .withStatus(response.getTransactionStatus().toTinkPaymentStatus())
                        .build(),
                paymentStorage);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();
        final PaymentProduct product = paymentProductForPayment(payment);
        final String paymentId = payment.getUniqueId();

        final GetPaymentResponse response = apiClient.fetchPayment(paymentId, product);

        Payment.Builder builder =
                new Payment.Builder()
                        .withCreditor(new Creditor(response.getCreditorAccount()))
                        .withDebtor(new Debtor(response.getDebtorAccount()))
                        .withAmount(response.getAmount())
                        .withExecutionDate(response.getRequestedExecutionDate())
                        .withCurrency(response.getCurrency())
                        .withUniqueId(paymentId)
                        .withType(product.getPaymentType())
                        .withStatus(response.getTransactionStatus().toTinkPaymentStatus());

        response.getRemittanceInformationUnstructured()
                .ifPresent(
                        remittanceInformation ->
                                builder.withReference(new Reference(null, remittanceInformation)));

        return new PaymentResponse(builder.build(), Storage.copyOf(paymentRequest.getStorage()));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        Payment payment = paymentMultiStepRequest.getPayment();
        final String paymentId = payment.getUniqueId();
        final PaymentProduct paymentProduct = paymentProductForPayment(payment);

        if (paymentMultiStepRequest
                .getStep()
                .equalsIgnoreCase(AuthenticationStepConstants.STEP_INIT)) {
            final Storage paymentStorage = paymentMultiStepRequest.getStorage();
            final String scaRedirectUrl = paymentStorage.get(StorageKeys.SCA_REDIRECT);
            final String scaToken = paymentStorage.get(StorageKeys.SCA_STATE);

            if (!scaRedirectHandler.handleRedirect(new URL(scaRedirectUrl), scaToken).isPresent()) {
                throw new PaymentException("SCA timed out.");
            }

            final RedsysTransactionStatus transactionStatus =
                    apiClient.fetchPaymentStatus(paymentId, paymentProduct).getTransactionStatus();
            final PaymentStatus paymentStatus = transactionStatus.toTinkPaymentStatus();
            payment.setStatus(paymentStatus);
            if (!paymentStatus.equals(PaymentStatus.PAID)) {
                throw new PaymentException(
                        "Unexpected payment status: " + transactionStatus.toString());
            }

            return new PaymentMultiStepResponse(
                    payment, AuthenticationStepConstants.STEP_FINALIZE, null);
        }

        return null;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException("createBeneficiary not implemented");
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();
        final PaymentProduct product;
        try {
            product = paymentProductForPayment(payment);
        } catch (PaymentException e) {
            throw new IllegalStateException(e);
        }
        final String paymentId = payment.getUniqueId();
        apiClient.cancelPayment(paymentId, product);
        payment.setStatus(PaymentStatus.CANCELLED);
        return new PaymentResponse(payment);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        ArrayList<PaymentResponse> responses = new ArrayList<>();

        for (PaymentRequest paymentRequest : paymentListRequest.getPaymentRequestList()) {
            responses.add(fetch(paymentRequest));
        }

        return new PaymentListResponse(responses);
    }
}
